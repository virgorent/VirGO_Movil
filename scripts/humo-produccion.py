"""Pruebas de humo del MVP de VirGo contra una API desplegada.

Uso:
  python humo_produccion.py --base https://<host-de-la-api>             (modo seguro)
  python humo_produccion.py --base https://<host-de-la-api> --completo  (crea salón, cotización y reserva)

Modo seguro: cuenta y sesión (030), catálogo y detalle (005/006) y el bloque del evento
(007/014/016/017). No deja nada público. Al final borra invitado y pendiente y la cuenta
(DELETE /users/me la anonimiza).

Modo completo: además, salón (022), cotización (008), respuesta (024) y reserva (009). La API
no tiene forma de borrar ni despublicar un salón, ni de cancelar una reserva: el salón de
prueba QUEDA PUBLICADO en el catálogo. Solo debe correrse con el visto bueno de quien
administra la base, que lo borra después. El script imprime los IDs a borrar.

Las cuentas y contraseñas son de prueba, generadas al vuelo, y no se imprimen.
"""
import argparse
import json
import secrets
import sys
import time
import urllib.error
import urllib.request
import uuid
from datetime import date, timedelta

parser = argparse.ArgumentParser()
parser.add_argument('--base', required=True, help='URL base de la API, sin /api/v1')
parser.add_argument('--completo', action='store_true', help='también salón, cotización, respuesta y reserva')
args = parser.parse_args()
BASE = args.base.rstrip('/')
results, created = [], []


def call(method, path, body=None, token=None, headers=None):
    time.sleep(0.12)  # rate limit: 20 peticiones por segundo
    data = json.dumps(body).encode('utf-8') if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    if token:
        req.add_header('Authorization', 'Bearer ' + token)
    for k, v in (headers or {}).items():
        req.add_header(k, v)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            raw = resp.read().decode('utf-8')
            return resp.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as err:
        raw = err.read().decode('utf-8')
        try:
            return err.code, json.loads(raw) if raw else None
        except ValueError:
            return err.code, raw
    except (urllib.error.URLError, TimeoutError) as err:
        return 0, str(err)


def check(task, name, ok, detail=''):
    results.append((task, name, bool(ok), detail))
    return bool(ok)


def data(body):
    return body.get('data', body) if isinstance(body, dict) else (body or [])


def account(role=None):
    email = f'humo-{role or "cliente"}-{uuid.uuid4().hex[:8]}@virgo.test'
    password = 'Humo-' + secrets.token_urlsafe(12) + '9a'
    body = {'name': f'Prueba de humo {role or "cliente"}', 'email': email, 'password': password, 'phone': '+524420000000'}
    if role:
        body['role'] = role
    status, reg = call('POST', '/api/v1/auth/register', body)
    check('T3008', f'registro {role or "cliente"} 201', status == 201, status)
    status, login = call('POST', '/api/v1/auth/login', {'email': email, 'password': password, 'device': 'android'})
    check('T3008', f'login {role or "cliente"} 200', status == 200, status)
    return (login or {}).get('accessToken') if isinstance(login, dict) else None


today = date.today()
status, _ = call('GET', '/health')
check('T3008', 'health 200', status == 200, status)

client = account()
if client:
    status, me = call('GET', '/api/v1/users/me', token=client)
    check('T3008', 'GET /users/me 200 con rol client', status == 200 and (me or {}).get('role') == 'client', status)

# 005/006: catálogo y detalle de un salón publicado real (solo lectura)
status, catalog = call('GET', '/api/v1/venues?pageSize=20')
venues = data(catalog)
check('T606', 'catálogo público 200', status == 200, status)
if venues:
    vid = venues[0]['id']
    status, detail = call('GET', f'/api/v1/venues/{vid}', token=client)
    check('T606', 'detalle con sesión trae precio', status == 200 and (detail or {}).get('price'), status)
    status, guest_view = call('GET', f'/api/v1/venues/{vid}')
    check('T606', 'detalle sin sesión oculta el precio', status == 200 and not (guest_view or {}).get('price'), status)
else:
    check('T606', 'hay al menos un salón publicado', False, 'catálogo vacío')

# 007/014/016/017: evento privado del cliente de prueba
if client:
    event_body = {'name': 'Prueba de humo', 'eventType': 'Boda', 'date': str(today + timedelta(days=60)),
                  'guests': 50, 'budget': 50000.0, 'hours': 5}
    status, event = call('POST', '/api/v1/events', event_body, token=client)
    if check('T704', 'crear evento 201', status == 201, status):
        eid = event['id']
        created.append(('events', eid))
        status, listing = call('GET', '/api/v1/events', token=client)
        check('T1403', 'Mi Evento lo lista', status == 200 and any(e['id'] == eid for e in data(listing)), status)
        status, edited = call('PATCH', f'/api/v1/events/{eid}', dict(event_body, guests=60), token=client)
        check('T704', 'editar evento 200', status == 200 and (edited or {}).get('guests') == 60, status)
        status, guest = call('POST', f'/api/v1/events/{eid}/guests',
                             {'name': 'Invitado de prueba', 'phone': '4420000000', 'companions': 1, 'status': 'pending'},
                             token=client)
        if check('T1604', 'agregar invitado 201', status == 201, status):
            status, _ = call('DELETE', f'/api/v1/events/{eid}/guests/{guest["id"]}', token=client)
            check('T1604', 'borrar invitado 204', status in (200, 204), status)
        status, task = call('POST', f'/api/v1/events/{eid}/tasks',
                            {'title': 'Pendiente de prueba', 'dueDate': str(today + timedelta(days=30)), 'done': False},
                            token=client)
        if check('T1704', 'agregar pendiente 201', status == 201, status):
            status, done = call('PATCH', f'/api/v1/events/{eid}/tasks/{task["id"]}',
                                {'title': 'Pendiente de prueba', 'dueDate': str(today + timedelta(days=30)), 'done': True},
                                token=client)
            check('T1704', 'marcar pendiente hecho 200', status == 200 and (done or {}).get('done') is True, status)
            status, _ = call('DELETE', f'/api/v1/events/{eid}/tasks/{task["id"]}', token=client)
            check('T1704', 'borrar pendiente 204', status in (200, 204), status)
        status, _ = call('GET', f'/api/v1/events/{eid}', token=account())
        check('T704', 'otro cliente no ve el evento (404)', status == 404, status)

if args.completo and client:
    owner = account('owner')
    venue_body = {'name': '[PRUEBA DE HUMO] No reservar', 'shortName': 'Prueba de humo',
                  'description': 'Salón creado por la prueba de humo. Se borra después. No reservar.',
                  'address': 'Prueba de humo, Querétaro', 'zone': 'Centro', 'capacity': 50,
                  'price': {'base': 1000.0, 'unit': 'hour', 'minHours': 4},
                  'services': [], 'categories': ['Salon'], 'amenities': []}
    status, venue = call('POST', '/api/v1/venues', venue_body, token=owner)
    if check('T2208', 'crear salón 201', status == 201, status):
        vid = venue['id']
        created.append(('venues', vid))
        status, _ = call('POST', f'/api/v1/venues/{vid}/publish', token=owner)
        check('T2208', 'publicar salón 200', status == 200, status)
        event_date = str(today + timedelta(days=90))
        status, quote = call('POST', '/api/v1/quotes', {'target': {'type': 'venue', 'id': vid}, 'eventType': 'Boda',
                                                        'eventDate': event_date, 'guests': 40,
                                                        'message': 'Prueba de humo'}, token=client)
        if check('T808', 'cotizar 201 sent', status == 201 and quote.get('status') == 'sent', status):
            qid = quote['id']
            created.append(('quotes', qid))
            status, answered = call('POST', f'/api/v1/quotes/{qid}/response',
                                    {'total': 20000.0, 'deposit': 6000.0, 'message': 'Prueba de humo'}, token=owner)
            check('T2405', 'responder 200 answered', status == 200 and (answered or {}).get('status') == 'answered', status)
            status, booking = call('POST', '/api/v1/bookings', {'venueId': vid, 'date': event_date, 'hours': 4, 'quoteId': qid},
                                   token=client, headers={'Idempotency-Key': str(uuid.uuid4())})
            if check('T908', 'reservar con cotización 201', status == 201, status):
                created.append(('bookings', booking['id']))
                check('T908', 'total = cotizado y sin datos internos',
                      booking.get('total') == 20000 and 'clientId' not in booking and 'idempotencyKey' not in booking,
                      {k: booking.get(k) for k in ('total', 'deposit', 'status')})
    if owner:
        call('DELETE', '/api/v1/users/me', token=owner)

if client:
    status, _ = call('DELETE', '/api/v1/users/me', token=client)
    check('T3008', 'borrar la cuenta de prueba 204', status in (200, 204), status)
    status, _ = call('GET', '/api/v1/users/me', token=client)
    check('T3008', 'la sesión queda revocada (401)', status == 401, status)

failed = [r for r in results if not r[2]]
for task, name, ok, detail in results:
    print(('OK   ' if ok else 'FALLA'), f'{task:6} {name}', '' if ok else f'-> {detail}')
print(f'\n{len(results) - len(failed)}/{len(results)} comprobaciones en verde ({"completo" if args.completo else "seguro"})')
if created:
    print('\nCreado en la base (borrar si no aplica):')
    for collection, oid in created:
        print(f'  {collection}: {oid}')
sys.exit(1 if failed else 0)
