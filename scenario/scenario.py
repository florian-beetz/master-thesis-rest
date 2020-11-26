import requests
from pprint import pprint
import time

BASE_URL = 'http://host.docker.internal:8080'

REALM = 'ma-rest-shop'
CLIENT_ID = 'external'
CLIENT_SECRET = '84e9fbd1-24fb-46d8-a398-3c4a4f7e9c0e'

USERNAME = 'admin'
PASSWORD = 'admin'


def authenticate():
    # grant_type=password&client_id=external&client_secret=161f9398-7e03-4492-a5d9-2b4f1b4b96c7&username=someone&password=password
    payload = {
        'grant_type': 'password',
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET,
        'username': USERNAME,
        'password': PASSWORD,
    }
    r = requests.post(BASE_URL + "/auth/realms/{0}/protocol/openid-connect/token".format(REALM), data=payload)
    if r.status_code != 200:
        raise ValueError('Authentication failed with code: {0}'.format(r.status_code))
    return r.json()['access_token']


def create_warehouse(name, token):
    headers = {'authorization': 'Bearer {0}'.format(token)}
    payload = {'name': name}
    r = requests.post(BASE_URL + "/inventory/api/v1/warehouse/", headers=headers, json=payload)
    if r.status_code != 201:
        raise ValueError('Creating warehouse failed with code: {0}'.format(r.status_code))

    return r.headers.get('location')


def create_item(name, price, token):
    headers = {'authorization': 'Bearer {0}'.format(token)}
    payload = {'title': name, 'price': price}
    r = requests.post(BASE_URL + "/inventory/api/v1/item/", headers=headers, json=payload)
    if r.status_code != 201:
        raise ValueError('Creating item failed with code: {0}'.format(r.status_code))

    return r.headers.get('location')


def create_stock_position(item, warehouse, amount, token):
    headers = {'authorization': 'Bearer {0}'.format(token)}
    payload = {'inStock': amount, 'warehouse': warehouse}
    r = requests.post(item + "/stock/", headers=headers, json=payload)
    if r.status_code != 201:
        raise ValueError('Creating stock position failed with code: {0}'.format(r.status_code))

    return r.headers.get('location')


def create_order(items, houseNo):
    payload = {
        'items': [{'item': item, 'amount': amount} for item, amount in items.items()],
        'address': {
            'street': 'Street {0}'.format(houseNo),
            'city': 'City',
            'zip': '12345',
        },
        'status': 'created'
    }
    r = requests.post(BASE_URL + "/order/api/v1/order/", json=payload)
    if r.status_code != 201:
        raise ValueError('Creating order failed with code: {0}'.format(r.status_code))

    return r.headers.get('location')


def get_payment_of_order(order):
    r = requests.get(order)
    links = r.json()['_links']
    if not 'payment' in links:
        return None
    return links['payment']['href']


def get_payment_status_etag(payment):
    r = requests.get(payment + "/status")
    return r.headers.get('etag')


def update_payment_status(payment, status, etag, token):
    headers = {'authorization': 'Bearer {0}'.format(token), 'if-match': etag}
    r = requests.put(payment + "/status", headers=headers, data=status)
    pprint(r.status_code)


if __name__ == '__main__':
    token = authenticate()
    print("Using token to authenticate: {0}".format(token))
    warehouse = create_warehouse("Warehouse 1", token)
    item = create_item("Item 1", 3.50, token)
    create_stock_position(item, warehouse, 2000, token)

    order = create_order({item: 1}, 1)

    time.sleep(130)

    payment = get_payment_of_order(order)
    etag = get_payment_status_etag(payment)
    update_payment_status(payment, 'payed', etag, token)


