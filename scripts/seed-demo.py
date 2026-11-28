"""Datos de demostración para VirGo: 15 usuarios, 12 salones, cotizaciones, reservas y eventos.

Crea 3 propietarios con 4 salones publicados cada uno y 12 clientes. Cada cliente pide cotizaciones
en distintos estados (pendiente, respondida, reservada) y la mitad tiene un evento con invitados y
pendientes. Las contraseñas se generan al azar y se guardan en el archivo de salida, nunca en pantalla.

Uso:
    python scripts/seed-demo.py                        # backend local en http://127.0.0.1:5122
    python scripts/seed-demo.py --base <url> --permitir-remoto

Por seguridad solo habla con localhost salvo que se pase --permitir-remoto; contra una base compartida
úsalo solo con el visto bueno de quien la administra. Si los correos ya existen (409), se detiene: la
base ya tiene la demo. Requiere Python 3.10+, sin dependencias.
"""
import argparse
import json
import secrets
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from datetime import date, timedelta

# Fotos libres de Lorem Picsum; la semilla fija la misma imagen en cada corrida (036 RF-07).
PHOTO_URL = 'https://picsum.photos/seed/virgo-{slug}-{n}/1200/675'
LOCAL_HOSTS = {'127.0.0.1', 'localhost', '[::1]', '::1', '10.0.2.2'}
DOMAIN = 'virgo.test'

OWNERS = [
    ('Mariana Salones', 'mariana'),
    ('Roberto Eventos', 'roberto'),
    ('Sofía Jardines', 'sofia'),
]
CLIENTS = [
    'Ana Torres', 'Luis Hernández', 'Carla Méndez', 'Diego Ramírez', 'Valeria Ortiz', 'Jorge Castillo',
    'Fernanda Ríos', 'Miguel Ángel Soto', 'Paola Jiménez', 'Ricardo Vega', 'Daniela Cruz', 'Andrés Morales',
]
# nombre, corto, zona, capacidad, precio, unidad, horas mínimas, descripción, amenidades, categoría
VENUES = [
    ('Jardín Los Encinos', 'Los Encinos', 'Juriquilla', 250, 3500, 'hour', 5,
     'Jardín al aire libre con terraza techada, ideal para bodas y XV años.', ['Estacionamiento', 'Cocina'], 'Jardin'),
    ('Salón Cristal', 'Cristal', 'Centro', 180, 28000, 'day', 1,
     'Salón climatizado en el centro histórico, con pista de baile y mobiliario.', ['Aire acondicionado', 'Pista de baile'], 'Salon'),
    ('Terraza Bella Vista', 'Bella Vista', 'El Refugio', 120, 2200, 'hour', 4,
     'Terraza con vista panorámica para eventos sociales y corporativos.', ['Estacionamiento', 'Sonido'], 'Terraza'),
    ('Hacienda San Gil', 'San Gil', 'Corregidora', 400, 65000, 'day', 1,
     'Hacienda con capilla y jardines amplios para bodas grandes.', ['Estacionamiento', 'Cocina', 'Sonido'], 'Hacienda'),
    ('Salón Imperial', 'Imperial', 'Santa Fe', 300, 4200, 'hour', 5,
     'Salón con candelabros, escenario y cocina industrial.', ['Cocina', 'Aire acondicionado', 'Pantalla LED'], 'Salon de eventos'),
    ('Quinta Las Rosas', 'Las Rosas', 'Corregidora', 200, 3000, 'hour', 4,
     'Quinta con jardín y área infantil, perfecta para fiestas familiares.', ['Estacionamiento', 'Sonido'], 'Jardin'),
    ('Loft Alameda', 'Alameda', 'Centro', 80, 1800, 'hour', 3,
     'Espacio industrial para presentaciones, cócteles y sesiones de fotos.', ['Pantalla LED', 'Sonido'], 'Salon'),
    ('Jardín del Bosque', 'Del Bosque', 'Jurica', 350, 4800, 'hour', 6,
     'Jardín rodeado de árboles con iluminación cálida y pérgola.', ['Estacionamiento', 'Pista de baile'], 'Jardin'),
    ('Salón Diamante', 'Diamante', 'El Marqués', 220, 32000, 'day', 1,
     'Salón con pista iluminada, barra y vestidor para quinceañeras.', ['Pista de baile', 'Aire acondicionado', 'Sonido'], 'Salon de eventos'),
    ('Rooftop Cielo Azul', 'Cielo Azul', 'Juriquilla', 100, 2500, 'hour', 4,
     'Rooftop con vista a la presa, para cenas y eventos íntimos.', ['Sonido', 'Pantalla LED'], 'Rooftop'),
    ('Casona Antea', 'Casona', 'Antea', 150, 3800, 'hour', 5,
     'Casona con patio central y arcos de cantera.', ['Cocina', 'Estacionamiento'], 'Hacienda'),
    ('Terraza Jurica', 'Jurica', 'Jurica', 500, 85000, 'day', 1,
     'Terraza y jardín con capacidad para eventos grandes.', ['Estacionamiento', 'Cocina', 'Pista de baile', 'Sonido'], 'Terraza'),
]
# Tipos que acepta la API; el segundo valor es el nombre del evento.
EVENT_TYPES = [('Boda', 'Boda'), ('XV años', 'XV años'), ('Cumpleaños', 'Cumpleaños'), ('Corporativo', 'Evento de empresa'),
               ('Otro', 'Graduación')]
GUESTS = [('María Pérez', 'confirmed', 1, '3'), ('Jorge Ruiz', 'pending', 0, None), ('Lucía Gómez', 'declined', 2, None),
          ('Pedro Sánchez', 'confirmed', 3, '1')]
TASKS = [('Contratar fotógrafo', 30, False), ('Pagar anticipo del salón', -2, False), ('Elegir menú', 10, True),
         ('Enviar invitaciones', 15, False)]


class Api:
    def __init__(self, base):
        self.base = base.rstrip('/')

    def call(self, method, path, body=None, token=None, headers=None):
        time.sleep(0.08)
        data = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(self.base + path, data=data, method=method)
        req.add_header('Content-Type', 'application/json')
        if token:
            req.add_header('Authorization', 'Bearer ' + token)
        for key, value in (headers or {}).items():
            req.add_header(key, value)
        with urllib.request.urlopen(req, timeout=20) as response:
            raw = response.read().decode()
            return json.loads(raw) if raw else None

    def account(self, name, email, role):
        password = 'Demo-' + secrets.token_hex(5) + 'a9'
        body = {'name': name, 'email': email, 'password': password, 'phone': '+524421234567', 'role': role}
        self.call('POST', '/api/v1/auth/register', body)
        login = self.call('POST', '/api/v1/auth/login', {'email': email, 'password': password, 'device': 'android'})
        return login['accessToken'], password


def day(offset):
    return str(date.today() + timedelta(days=offset))


def photos(short):
    slug = ''.join(ch for ch in short.lower() if ch.isalnum())
    return [{'url': PHOTO_URL.format(slug=slug, n=n), 'order': n} for n in range(3)]


def create_venues(api, owner_token, specs):
    venues = []
    for name, short, zone, cap, price, unit, hours, desc, amenities, category in specs:
        venue = api.call('POST', '/api/v1/venues', {
            'name': name, 'shortName': short, 'description': desc, 'address': f'{zone}, Querétaro', 'zone': zone,
            'capacity': cap, 'price': {'base': float(price), 'unit': unit, 'minHours': hours},
            'services': [], 'categories': [category], 'amenities': amenities,
            'photos': photos(short),
        }, owner_token)
        api.call('POST', f"/api/v1/venues/{venue['id']}/publish", token=owner_token)
        venue['ownerToken'] = owner_token
        venue['capacity'] = cap
        venue['minHours'] = hours
        venues.append(venue)
    return venues


def quote(api, client_token, venue, event_type, offset, guests):
    return api.call('POST', '/api/v1/quotes', {
        'target': {'type': 'venue', 'id': venue['id']}, 'eventType': event_type, 'eventDate': day(offset),
        'guests': guests, 'message': f'{event_type} para {guests} invitados, ¿tienen fecha?',
    }, client_token)


def answer(api, venue, quote_id, total):
    api.call('POST', f'/api/v1/quotes/{quote_id}/response',
             {'total': float(total), 'deposit': float(total // 4), 'message': 'Incluye mobiliario, meseros y limpieza.'},
             venue['ownerToken'])


def seed_client(api, index, client_token, venues, counts):
    """Tres cotizaciones por cliente: reservada, respondida y pendiente, en salones distintos."""
    event_type, label = EVENT_TYPES[index % len(EVENT_TYPES)]
    picks = [venues[(index + k * 5) % len(venues)] for k in range(3)]
    offset = 30 + index * 11
    guests = min(picks[0]['capacity'], 60 + index * 15)

    booked = quote(api, client_token, picks[0], event_type, offset, guests)
    answer(api, picks[0], booked['id'], 20000 + index * 3500)
    api.call('POST', '/api/v1/bookings', {'venueId': picks[0]['id'], 'date': day(offset), 'hours': max(5, picks[0]['minHours']), 'quoteId': booked['id']},
             client_token, {'Idempotency-Key': str(uuid.uuid4())})
    counts['bookings'] += 1

    answered = quote(api, client_token, picks[1], event_type, offset + 3, min(picks[1]['capacity'], guests))
    answer(api, picks[1], answered['id'], 18000 + index * 2500)
    quote(api, client_token, picks[2], event_type, offset + 7, min(picks[2]['capacity'], guests))
    counts['quotes'] += 3

    if index % 2 == 0:
        event = api.call('POST', '/api/v1/events', {
            'name': f'{label} de {CLIENTS[index].split()[0]}', 'eventType': event_type, 'date': day(offset),
            'guests': guests, 'budget': float(60000 + index * 10000), 'hours': max(5, picks[0]['minHours']), 'venueId': picks[0]['id'],
        }, client_token)
        for name, status, companions, table in GUESTS:
            body = {'name': name, 'phone': '4421112233', 'companions': companions, 'status': status}
            if table:
                body['table'] = table
            api.call('POST', f"/api/v1/events/{event['id']}/guests", body, client_token)
        for title, due, done in TASKS:
            api.call('POST', f"/api/v1/events/{event['id']}/tasks", {'title': title, 'dueDate': day(due), 'done': done}, client_token)
        counts['events'] += 1


def main():
    parser = argparse.ArgumentParser(description='Carga datos de demostración en una API de VirGo.')
    parser.add_argument('--base', default='http://127.0.0.1:5122', help='URL de la API (predeterminado: backend local)')
    parser.add_argument('--permitir-remoto', action='store_true', help='Permite una API que no sea localhost')
    parser.add_argument('--salida', default='seed-cuentas.local.txt', help='Archivo donde se guardan las cuentas')
    args = parser.parse_args()

    host = urllib.parse.urlparse(args.base).hostname or ''
    if host not in LOCAL_HOSTS and not args.permitir_remoto:
        sys.exit(f'{host} no es local. Usa --permitir-remoto solo con el visto bueno de quien administra esa base.')

    api = Api(args.base)
    accounts = []
    counts = {'quotes': 0, 'bookings': 0, 'events': 0}
    try:
        venues = []
        for index, (name, slug) in enumerate(OWNERS):
            email = f'{slug}.propietario@{DOMAIN}'
            token, password = api.account(name, email, 'owner')
            accounts.append(('Propietario', name, email, password))
            venues += create_venues(api, token, VENUES[index * 4:(index + 1) * 4])
        for index, name in enumerate(CLIENTS):
            email = f'cliente{index + 1:02d}@{DOMAIN}'
            token, password = api.account(name, email, 'client')
            accounts.append(('Cliente', name, email, password))
            seed_client(api, index, token, venues, counts)
    except urllib.error.HTTPError as error:
        if error.code == 409:
            sys.exit('Un correo de la demo ya existe (409): esta base ya tiene la demo.')
        sys.exit(f'La API respondió {error.code} en {error.url}: {error.read().decode()[:300]}')
    finally:
        if accounts:
            with open(args.salida, 'w', encoding='utf-8') as out:
                out.write(f'Cuentas de demostración en {args.base}. No subir a git.\n\n')
                for role, name, email, password in accounts:
                    out.write(f'{role:<12} {name:<20} {email:<34} {password}\n')

    print(f"Listo: {len(accounts)} usuarios, {len(venues)} salones, {counts['quotes']} cotizaciones, "
          f"{counts['bookings']} reservas y {counts['events']} eventos. Cuentas en {args.salida}.")


if __name__ == '__main__':
    main()
