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
    warehouse1 = create_warehouse("Warehouse 1", token)
    warehouse2 = create_warehouse("Warehouse 2", token)
    warehouse3 = create_warehouse("Warehouse 3", token)

    item1 = create_item("Item 1", 2.99, token)
    item2 = create_item("Item 2", 9.99, token)
    item3 = create_item("Item 3", 1.49, token)
    item4 = create_item("Item 4", 7.59, token)
    item5 = create_item("Item 5", 11.99, token)

    create_stock_position(item1, warehouse1, 1000, token)
    create_stock_position(item1, warehouse2, 1000, token)
    create_stock_position(item1, warehouse3, 1000, token)
    create_stock_position(item2, warehouse1, 1000, token)
    create_stock_position(item2, warehouse3, 1000, token)
    create_stock_position(item3, warehouse1, 1000, token)
    create_stock_position(item3, warehouse2, 1000, token)
    create_stock_position(item3, warehouse3, 1000, token)
    create_stock_position(item4, warehouse1, 1000, token)
    create_stock_position(item4, warehouse2, 1000, token)
    create_stock_position(item4, warehouse3, 1000, token)
    create_stock_position(item5, warehouse1, 1000, token)
    create_stock_position(item5, warehouse2, 1000, token)
    order1 = create_order({item4: 3}, 1)
    time.sleep(0.061)
    order2 = create_order({item4: 3}, 2)
    time.sleep(0.093)
    order3 = create_order({item1: 1, item5: 2, item3: 2}, 3)
    time.sleep(0.183)
    order4 = create_order({item1: 1, item4: 1, item3: 3, item5: 1}, 4)
    time.sleep(0.128)
    order5 = create_order({item4: 1, item1: 3}, 5)
    time.sleep(0.179)
    order6 = create_order({item5: 2, item2: 1, item4: 1, item1: 1}, 6)
    time.sleep(0.11)
    order7 = create_order({item2: 3}, 7)
    time.sleep(0.151)
    order8 = create_order({item3: 1, item2: 2}, 8)
    time.sleep(0.203)
    order9 = create_order({item3: 1, item2: 1, item1: 3}, 9)
    time.sleep(0.15)
    order10 = create_order({item3: 2}, 10)
    time.sleep(0.192)
    order11 = create_order({item2: 3, item3: 3}, 11)
    time.sleep(0.219)
    order12 = create_order({item1: 2}, 12)
    time.sleep(0.064)
    order13 = create_order({item4: 3, item1: 2}, 13)
    time.sleep(0.172)
    order14 = create_order({item4: 2, item5: 2}, 14)
    time.sleep(0.229)
    order15 = create_order({item3: 2}, 15)
    time.sleep(0.062)
    order16 = create_order({item2: 2}, 16)
    time.sleep(0.226)
    order17 = create_order({item4: 1, item3: 1, item2: 2, item1: 2}, 17)
    time.sleep(0.216)
    order18 = create_order({item3: 2}, 18)
    time.sleep(0.168)
    order19 = create_order({item5: 1, item1: 2, item4: 2, item3: 2}, 19)
    time.sleep(0.009)
    order20 = create_order({item1: 2, item2: 3, item5: 2, item3: 3}, 20)
    time.sleep(0.13)
    order21 = create_order({item5: 3}, 21)
    time.sleep(0.241)
    order22 = create_order({item4: 2}, 22)
    time.sleep(0.203)
    order23 = create_order({item2: 2, item4: 3, item3: 1, item1: 1}, 23)
    time.sleep(0.076)
    order24 = create_order({item3: 1, item1: 2}, 24)
    time.sleep(0.13)
    order25 = create_order({item2: 3, item4: 3, item1: 1, item3: 1}, 25)
    time.sleep(0.173)
    order26 = create_order({item1: 3, item2: 1}, 26)
    time.sleep(0.089)
    order27 = create_order({item4: 1, item1: 2, item3: 2}, 27)
    time.sleep(0.134)
    order28 = create_order({item4: 1, item5: 2}, 28)
    time.sleep(0.017)
    order29 = create_order({item3: 1, item5: 1}, 29)
    time.sleep(0.152)
    order30 = create_order({item4: 3, item2: 1, item3: 1}, 30)
    time.sleep(0.205)
    order31 = create_order({item5: 3}, 31)
    time.sleep(0.068)
    order32 = create_order({item5: 2, item3: 2}, 32)
    time.sleep(0.12)
    order33 = create_order({item3: 3}, 33)
    time.sleep(0.089)
    order34 = create_order({item4: 1}, 34)
    time.sleep(0.122)
    order35 = create_order({item2: 2, item3: 2, item5: 3}, 35)
    time.sleep(0.083)
    order36 = create_order({item2: 1, item5: 1, item4: 1}, 36)
    time.sleep(0.023)
    order37 = create_order({item2: 1, item5: 3}, 37)
    time.sleep(0.023)
    order38 = create_order({item3: 1, item2: 2, item4: 3, item5: 2}, 38)
    time.sleep(0.151)
    order39 = create_order({item3: 1, item1: 3, item5: 2, item2: 3}, 39)
    time.sleep(0.213)
    order40 = create_order({item2: 1, item4: 1, item3: 3, item5: 2}, 40)
    time.sleep(0.07)
    order41 = create_order({item2: 2, item1: 3, item5: 3, item3: 1}, 41)
    time.sleep(0.098)
    order42 = create_order({item4: 2, item5: 3, item3: 1}, 42)
    time.sleep(0.146)
    order43 = create_order({item1: 3, item4: 3, item2: 3}, 43)
    time.sleep(0.185)
    order44 = create_order({item3: 1}, 44)
    time.sleep(0.154)
    order45 = create_order({item2: 1, item5: 2}, 45)
    time.sleep(0.103)
    order46 = create_order({item1: 1, item2: 1, item4: 2}, 46)
    time.sleep(0.075)
    order47 = create_order({item1: 2, item5: 3, item4: 3, item3: 3}, 47)
    time.sleep(0.082)
    order48 = create_order({item4: 1, item3: 3, item1: 2}, 48)
    time.sleep(0.114)
    order49 = create_order({item4: 2, item5: 2, item3: 3}, 49)
    time.sleep(0.225)
    order50 = create_order({item3: 1}, 50)
    time.sleep(0.04)
    order51 = create_order({item1: 1}, 51)
    time.sleep(0.225)
    order52 = create_order({item1: 3, item4: 2, item5: 1, item2: 1}, 52)
    time.sleep(0.045)
    order53 = create_order({item3: 2, item2: 3, item1: 1}, 53)
    time.sleep(0.147)
    order54 = create_order({item5: 1, item4: 3, item2: 2, item1: 3}, 54)
    time.sleep(0.056)
    order55 = create_order({item5: 3, item1: 3}, 55)
    time.sleep(0.007)
    order56 = create_order({item2: 1, item1: 1, item3: 3, item5: 3}, 56)
    time.sleep(0.233)
    order57 = create_order({item4: 2, item1: 1, item3: 3}, 57)
    time.sleep(0.031)
    order58 = create_order({item4: 3, item5: 1, item3: 3}, 58)
    time.sleep(0.042)
    order59 = create_order({item2: 1, item1: 3, item4: 2}, 59)
    time.sleep(0.245)
    order60 = create_order({item3: 3, item1: 1, item4: 3, item2: 3}, 60)
    time.sleep(0.219)
    order61 = create_order({item5: 2, item1: 1, item4: 3}, 61)
    time.sleep(0.067)
    order62 = create_order({item1: 3}, 62)
    time.sleep(0.16)
    order63 = create_order({item4: 2, item5: 2}, 63)
    time.sleep(0.079)
    order64 = create_order({item3: 1, item1: 3}, 64)
    time.sleep(0.127)
    order65 = create_order({item3: 1, item5: 1, item4: 3}, 65)
    time.sleep(0.044)
    order66 = create_order({item3: 2, item4: 3, item2: 2}, 66)
    time.sleep(0.223)
    order67 = create_order({item1: 1}, 67)
    time.sleep(0.217)
    order68 = create_order({item1: 1, item5: 3, item2: 1, item4: 1}, 68)
    time.sleep(0.179)
    order69 = create_order({item5: 3}, 69)
    time.sleep(0.082)
    order70 = create_order({item5: 2, item1: 3, item3: 1, item2: 3}, 70)
    time.sleep(0.089)
    order71 = create_order({item5: 1, item4: 3, item3: 3}, 71)
    time.sleep(0.102)
    order72 = create_order({item3: 3, item4: 3}, 72)
    time.sleep(0.017)
    order73 = create_order({item3: 2, item2: 1, item5: 2, item4: 2}, 73)
    time.sleep(0.203)
    order74 = create_order({item2: 1, item4: 3}, 74)
    time.sleep(0.112)
    order75 = create_order({item3: 3, item5: 3}, 75)
    time.sleep(0.019)
    order76 = create_order({item1: 2, item2: 3}, 76)
    time.sleep(0.043)
    order77 = create_order({item4: 3, item3: 2}, 77)
    time.sleep(0.234)
    order78 = create_order({item4: 1}, 78)
    time.sleep(0.114)
    order79 = create_order({item3: 1}, 79)
    time.sleep(0.182)
    order80 = create_order({item5: 1, item4: 2}, 80)
    time.sleep(0.061)
    order81 = create_order({item2: 3, item1: 3}, 81)
    time.sleep(0.124)
    order82 = create_order({item4: 2, item3: 3, item5: 2}, 82)
    time.sleep(0.088)
    order83 = create_order({item4: 2, item5: 3, item1: 1}, 83)
    time.sleep(0.233)
    order84 = create_order({item5: 3, item3: 2, item4: 2, item1: 3}, 84)
    time.sleep(0.218)
    order85 = create_order({item2: 1}, 85)
    time.sleep(0.036)
    order86 = create_order({item5: 3, item1: 2, item3: 1}, 86)
    time.sleep(0.065)
    order87 = create_order({item2: 3}, 87)
    time.sleep(0.209)
    order88 = create_order({item5: 3, item2: 2, item4: 2, item1: 1}, 88)
    time.sleep(0.247)
    order89 = create_order({item2: 2, item5: 2}, 89)
    time.sleep(0.243)
    order90 = create_order({item1: 3}, 90)
    time.sleep(0.153)
    order91 = create_order({item4: 3, item1: 3, item5: 3}, 91)
    time.sleep(0.18)
    order92 = create_order({item3: 1, item1: 1, item5: 3}, 92)
    time.sleep(0.223)
    order93 = create_order({item1: 1, item2: 1, item4: 2}, 93)
    time.sleep(0.123)
    order94 = create_order({item3: 3, item1: 1, item5: 2}, 94)
    time.sleep(0.167)
    order95 = create_order({item3: 2, item2: 3}, 95)
    time.sleep(0.238)
    order96 = create_order({item5: 3, item3: 2, item2: 1}, 96)
    time.sleep(0.206)
    order97 = create_order({item1: 3}, 97)
    time.sleep(0.187)
    order98 = create_order({item5: 3, item2: 3}, 98)
    time.sleep(0.233)
    order99 = create_order({item4: 2, item2: 3, item5: 1, item3: 3}, 99)
    time.sleep(0.246)
    order100 = create_order({item1: 2, item2: 2, item5: 1}, 100)
    time.sleep(0.167)

    time.sleep(120)

    payment63 = get_payment_of_order(order63)
    etag63 = get_payment_status_etag(payment63)
    update_payment_status(payment63, 'payed', etag63, token)
    time.sleep(0.213)
    payment3 = get_payment_of_order(order3)
    etag3 = get_payment_status_etag(payment3)
    update_payment_status(payment3, 'payed', etag3, token)
    time.sleep(0.087)
    payment35 = get_payment_of_order(order35)
    etag35 = get_payment_status_etag(payment35)
    update_payment_status(payment35, 'payed', etag35, token)
    time.sleep(0.234)
    payment15 = get_payment_of_order(order15)
    etag15 = get_payment_status_etag(payment15)
    update_payment_status(payment15, 'payed', etag15, token)
    time.sleep(0.047)
    payment64 = get_payment_of_order(order64)
    etag64 = get_payment_status_etag(payment64)
    update_payment_status(payment64, 'payed', etag64, token)
    time.sleep(0.099)
    payment88 = get_payment_of_order(order88)
    etag88 = get_payment_status_etag(payment88)
    update_payment_status(payment88, 'payed', etag88, token)
    time.sleep(0.123)
    payment66 = get_payment_of_order(order66)
    etag66 = get_payment_status_etag(payment66)
    update_payment_status(payment66, 'payed', etag66, token)
    time.sleep(0.045)
    payment43 = get_payment_of_order(order43)
    etag43 = get_payment_status_etag(payment43)
    update_payment_status(payment43, 'payed', etag43, token)
    time.sleep(0.145)
    payment23 = get_payment_of_order(order23)
    etag23 = get_payment_status_etag(payment23)
    update_payment_status(payment23, 'payed', etag23, token)
    time.sleep(0.23)
    payment62 = get_payment_of_order(order62)
    etag62 = get_payment_status_etag(payment62)
    update_payment_status(payment62, 'payed', etag62, token)
    time.sleep(0.217)
    payment26 = get_payment_of_order(order26)
    etag26 = get_payment_status_etag(payment26)
    update_payment_status(payment26, 'payed', etag26, token)
    time.sleep(0.244)
    payment19 = get_payment_of_order(order19)
    etag19 = get_payment_status_etag(payment19)
    update_payment_status(payment19, 'payed', etag19, token)
    time.sleep(0.013)
    payment90 = get_payment_of_order(order90)
    etag90 = get_payment_status_etag(payment90)
    update_payment_status(payment90, 'payed', etag90, token)
    time.sleep(0.195)
    payment72 = get_payment_of_order(order72)
    etag72 = get_payment_status_etag(payment72)
    update_payment_status(payment72, 'payed', etag72, token)
    time.sleep(0.103)
    payment13 = get_payment_of_order(order13)
    etag13 = get_payment_status_etag(payment13)
    update_payment_status(payment13, 'payed', etag13, token)
    time.sleep(0.198)
    payment73 = get_payment_of_order(order73)
    etag73 = get_payment_status_etag(payment73)
    update_payment_status(payment73, 'payed', etag73, token)
    time.sleep(0.069)
    payment76 = get_payment_of_order(order76)
    etag76 = get_payment_status_etag(payment76)
    update_payment_status(payment76, 'payed', etag76, token)
    time.sleep(0.123)
    payment38 = get_payment_of_order(order38)
    etag38 = get_payment_status_etag(payment38)
    update_payment_status(payment38, 'payed', etag38, token)
    time.sleep(0.205)
    payment24 = get_payment_of_order(order24)
    etag24 = get_payment_status_etag(payment24)
    update_payment_status(payment24, 'payed', etag24, token)
    time.sleep(0.159)
    payment80 = get_payment_of_order(order80)
    etag80 = get_payment_status_etag(payment80)
    update_payment_status(payment80, 'payed', etag80, token)
    time.sleep(0.221)
    payment79 = get_payment_of_order(order79)
    etag79 = get_payment_status_etag(payment79)
    update_payment_status(payment79, 'payed', etag79, token)
    time.sleep(0.105)
    payment7 = get_payment_of_order(order7)
    etag7 = get_payment_status_etag(payment7)
    update_payment_status(payment7, 'payed', etag7, token)
    time.sleep(0.151)
    payment78 = get_payment_of_order(order78)
    etag78 = get_payment_status_etag(payment78)
    update_payment_status(payment78, 'payed', etag78, token)
    time.sleep(0.032)
    payment61 = get_payment_of_order(order61)
    etag61 = get_payment_status_etag(payment61)
    update_payment_status(payment61, 'payed', etag61, token)
    time.sleep(0.125)
    payment14 = get_payment_of_order(order14)
    etag14 = get_payment_status_etag(payment14)
    update_payment_status(payment14, 'payed', etag14, token)
    time.sleep(0.034)
    payment1 = get_payment_of_order(order1)
    etag1 = get_payment_status_etag(payment1)
    update_payment_status(payment1, 'payed', etag1, token)
    time.sleep(0.005)
    payment94 = get_payment_of_order(order94)
    etag94 = get_payment_status_etag(payment94)
    update_payment_status(payment94, 'payed', etag94, token)
    time.sleep(0.047)
    payment51 = get_payment_of_order(order51)
    etag51 = get_payment_status_etag(payment51)
    update_payment_status(payment51, 'payed', etag51, token)
    time.sleep(0.121)
    payment42 = get_payment_of_order(order42)
    etag42 = get_payment_status_etag(payment42)
    update_payment_status(payment42, 'payed', etag42, token)
    time.sleep(0.036)
    payment37 = get_payment_of_order(order37)
    etag37 = get_payment_status_etag(payment37)
    update_payment_status(payment37, 'payed', etag37, token)
    time.sleep(0.038)
    payment9 = get_payment_of_order(order9)
    etag9 = get_payment_status_etag(payment9)
    update_payment_status(payment9, 'payed', etag9, token)
    time.sleep(0.136)
    payment75 = get_payment_of_order(order75)
    etag75 = get_payment_status_etag(payment75)
    update_payment_status(payment75, 'payed', etag75, token)
    time.sleep(0.222)
    payment67 = get_payment_of_order(order67)
    etag67 = get_payment_status_etag(payment67)
    update_payment_status(payment67, 'payed', etag67, token)
    time.sleep(0.189)
    payment57 = get_payment_of_order(order57)
    etag57 = get_payment_status_etag(payment57)
    update_payment_status(payment57, 'payed', etag57, token)
    time.sleep(0.222)
    payment95 = get_payment_of_order(order95)
    etag95 = get_payment_status_etag(payment95)
    update_payment_status(payment95, 'payed', etag95, token)
    time.sleep(0.045)
    payment56 = get_payment_of_order(order56)
    etag56 = get_payment_status_etag(payment56)
    update_payment_status(payment56, 'payed', etag56, token)
    time.sleep(0.078)
    payment39 = get_payment_of_order(order39)
    etag39 = get_payment_status_etag(payment39)
    update_payment_status(payment39, 'payed', etag39, token)
    time.sleep(0.209)
    payment8 = get_payment_of_order(order8)
    etag8 = get_payment_status_etag(payment8)
    update_payment_status(payment8, 'payed', etag8, token)
    time.sleep(0.239)
    payment34 = get_payment_of_order(order34)
    etag34 = get_payment_status_etag(payment34)
    update_payment_status(payment34, 'payed', etag34, token)
    time.sleep(0.038)
    payment81 = get_payment_of_order(order81)
    etag81 = get_payment_status_etag(payment81)
    update_payment_status(payment81, 'payed', etag81, token)
    time.sleep(0.027)
    payment16 = get_payment_of_order(order16)
    etag16 = get_payment_status_etag(payment16)
    update_payment_status(payment16, 'payed', etag16, token)
    time.sleep(0.161)
    payment47 = get_payment_of_order(order47)
    etag47 = get_payment_status_etag(payment47)
    update_payment_status(payment47, 'payed', etag47, token)
    time.sleep(0.139)
    payment29 = get_payment_of_order(order29)
    etag29 = get_payment_status_etag(payment29)
    update_payment_status(payment29, 'payed', etag29, token)
    time.sleep(0.187)
    payment28 = get_payment_of_order(order28)
    etag28 = get_payment_status_etag(payment28)
    update_payment_status(payment28, 'payed', etag28, token)
    time.sleep(0.18)
    payment4 = get_payment_of_order(order4)
    etag4 = get_payment_status_etag(payment4)
    update_payment_status(payment4, 'payed', etag4, token)
    time.sleep(0.074)
    payment40 = get_payment_of_order(order40)
    etag40 = get_payment_status_etag(payment40)
    update_payment_status(payment40, 'payed', etag40, token)
    time.sleep(0.033)
    payment12 = get_payment_of_order(order12)
    etag12 = get_payment_status_etag(payment12)
    update_payment_status(payment12, 'payed', etag12, token)
    time.sleep(0.084)
    payment30 = get_payment_of_order(order30)
    etag30 = get_payment_status_etag(payment30)
    update_payment_status(payment30, 'payed', etag30, token)
    time.sleep(0.112)
    payment17 = get_payment_of_order(order17)
    etag17 = get_payment_status_etag(payment17)
    update_payment_status(payment17, 'payed', etag17, token)
    time.sleep(0.114)
    payment45 = get_payment_of_order(order45)
    etag45 = get_payment_status_etag(payment45)
    update_payment_status(payment45, 'payed', etag45, token)
    time.sleep(0.005)
    payment92 = get_payment_of_order(order92)
    etag92 = get_payment_status_etag(payment92)
    update_payment_status(payment92, 'payed', etag92, token)
    time.sleep(0.005)
    payment87 = get_payment_of_order(order87)
    etag87 = get_payment_status_etag(payment87)
    update_payment_status(payment87, 'payed', etag87, token)
    time.sleep(0.009)
    payment74 = get_payment_of_order(order74)
    etag74 = get_payment_status_etag(payment74)
    update_payment_status(payment74, 'payed', etag74, token)
    time.sleep(0.04)
    payment69 = get_payment_of_order(order69)
    etag69 = get_payment_status_etag(payment69)
    update_payment_status(payment69, 'payed', etag69, token)
    time.sleep(0.226)
    payment91 = get_payment_of_order(order91)
    etag91 = get_payment_status_etag(payment91)
    update_payment_status(payment91, 'payed', etag91, token)
    time.sleep(0.07)
    payment54 = get_payment_of_order(order54)
    etag54 = get_payment_status_etag(payment54)
    update_payment_status(payment54, 'payed', etag54, token)
    time.sleep(0.2)
    payment36 = get_payment_of_order(order36)
    etag36 = get_payment_status_etag(payment36)
    update_payment_status(payment36, 'payed', etag36, token)
    time.sleep(0.202)
    payment5 = get_payment_of_order(order5)
    etag5 = get_payment_status_etag(payment5)
    update_payment_status(payment5, 'payed', etag5, token)
    time.sleep(0.174)
    payment97 = get_payment_of_order(order97)
    etag97 = get_payment_status_etag(payment97)
    update_payment_status(payment97, 'payed', etag97, token)
    time.sleep(0.19)
    payment48 = get_payment_of_order(order48)
    etag48 = get_payment_status_etag(payment48)
    update_payment_status(payment48, 'payed', etag48, token)
    time.sleep(0.027)
    payment27 = get_payment_of_order(order27)
    etag27 = get_payment_status_etag(payment27)
    update_payment_status(payment27, 'payed', etag27, token)
    time.sleep(0.007)
    payment86 = get_payment_of_order(order86)
    etag86 = get_payment_status_etag(payment86)
    update_payment_status(payment86, 'payed', etag86, token)
    time.sleep(0.022)
    payment53 = get_payment_of_order(order53)
    etag53 = get_payment_status_etag(payment53)
    update_payment_status(payment53, 'payed', etag53, token)
    time.sleep(0.112)
    payment89 = get_payment_of_order(order89)
    etag89 = get_payment_status_etag(payment89)
    update_payment_status(payment89, 'payed', etag89, token)
    time.sleep(0.241)
    payment77 = get_payment_of_order(order77)
    etag77 = get_payment_status_etag(payment77)
    update_payment_status(payment77, 'payed', etag77, token)
    time.sleep(0.115)
    payment46 = get_payment_of_order(order46)
    etag46 = get_payment_status_etag(payment46)
    update_payment_status(payment46, 'payed', etag46, token)
    time.sleep(0.109)
    payment32 = get_payment_of_order(order32)
    etag32 = get_payment_status_etag(payment32)
    update_payment_status(payment32, 'payed', etag32, token)
    time.sleep(0.242)
    payment70 = get_payment_of_order(order70)
    etag70 = get_payment_status_etag(payment70)
    update_payment_status(payment70, 'payed', etag70, token)
    time.sleep(0.221)
    payment55 = get_payment_of_order(order55)
    etag55 = get_payment_status_etag(payment55)
    update_payment_status(payment55, 'payed', etag55, token)
    time.sleep(0.041)
    payment33 = get_payment_of_order(order33)
    etag33 = get_payment_status_etag(payment33)
    update_payment_status(payment33, 'payed', etag33, token)
    time.sleep(0.232)
    payment44 = get_payment_of_order(order44)
    etag44 = get_payment_status_etag(payment44)
    update_payment_status(payment44, 'payed', etag44, token)
    time.sleep(0.001)
    payment98 = get_payment_of_order(order98)
    etag98 = get_payment_status_etag(payment98)
    update_payment_status(payment98, 'payed', etag98, token)
    time.sleep(0.108)
    payment52 = get_payment_of_order(order52)
    etag52 = get_payment_status_etag(payment52)
    update_payment_status(payment52, 'payed', etag52, token)
    time.sleep(0.24)
    payment82 = get_payment_of_order(order82)
    etag82 = get_payment_status_etag(payment82)
    update_payment_status(payment82, 'payed', etag82, token)
    time.sleep(0.215)
    payment71 = get_payment_of_order(order71)
    etag71 = get_payment_status_etag(payment71)
    update_payment_status(payment71, 'payed', etag71, token)
    time.sleep(0.124)
    payment18 = get_payment_of_order(order18)
    etag18 = get_payment_status_etag(payment18)
    update_payment_status(payment18, 'payed', etag18, token)
    time.sleep(0.077)
    payment68 = get_payment_of_order(order68)
    etag68 = get_payment_status_etag(payment68)
    update_payment_status(payment68, 'payed', etag68, token)
    time.sleep(0.074)
    payment10 = get_payment_of_order(order10)
    etag10 = get_payment_status_etag(payment10)
    update_payment_status(payment10, 'payed', etag10, token)
    time.sleep(0.002)
    payment31 = get_payment_of_order(order31)
    etag31 = get_payment_status_etag(payment31)
    update_payment_status(payment31, 'payed', etag31, token)
    time.sleep(0.14)
    payment96 = get_payment_of_order(order96)
    etag96 = get_payment_status_etag(payment96)
    update_payment_status(payment96, 'payed', etag96, token)
    time.sleep(0.069)
    payment6 = get_payment_of_order(order6)
    etag6 = get_payment_status_etag(payment6)
    update_payment_status(payment6, 'payed', etag6, token)
    time.sleep(0.073)
    payment65 = get_payment_of_order(order65)
    etag65 = get_payment_status_etag(payment65)
    update_payment_status(payment65, 'payed', etag65, token)
    time.sleep(0.031)
    payment21 = get_payment_of_order(order21)
    etag21 = get_payment_status_etag(payment21)
    update_payment_status(payment21, 'payed', etag21, token)
    time.sleep(0.086)
    payment60 = get_payment_of_order(order60)
    etag60 = get_payment_status_etag(payment60)
    update_payment_status(payment60, 'payed', etag60, token)
    time.sleep(0.185)
    payment83 = get_payment_of_order(order83)
    etag83 = get_payment_status_etag(payment83)
    update_payment_status(payment83, 'payed', etag83, token)
    time.sleep(0.026)
    payment20 = get_payment_of_order(order20)
    etag20 = get_payment_status_etag(payment20)
    update_payment_status(payment20, 'payed', etag20, token)
    time.sleep(0.131)
    payment22 = get_payment_of_order(order22)
    etag22 = get_payment_status_etag(payment22)
    update_payment_status(payment22, 'payed', etag22, token)
    time.sleep(0.036)
    payment100 = get_payment_of_order(order100)
    etag100 = get_payment_status_etag(payment100)
    update_payment_status(payment100, 'payed', etag100, token)
    time.sleep(0.101)
    payment49 = get_payment_of_order(order49)
    etag49 = get_payment_status_etag(payment49)
    update_payment_status(payment49, 'payed', etag49, token)
    time.sleep(0.225)
    payment11 = get_payment_of_order(order11)
    etag11 = get_payment_status_etag(payment11)
    update_payment_status(payment11, 'payed', etag11, token)
    time.sleep(0.225)
    payment25 = get_payment_of_order(order25)
    etag25 = get_payment_status_etag(payment25)
    update_payment_status(payment25, 'payed', etag25, token)
    time.sleep(0.227)
    payment50 = get_payment_of_order(order50)
    etag50 = get_payment_status_etag(payment50)
    update_payment_status(payment50, 'payed', etag50, token)
    time.sleep(0.009)
    payment93 = get_payment_of_order(order93)
    etag93 = get_payment_status_etag(payment93)
    update_payment_status(payment93, 'payed', etag93, token)
    time.sleep(0.1)
    payment41 = get_payment_of_order(order41)
    etag41 = get_payment_status_etag(payment41)
    update_payment_status(payment41, 'payed', etag41, token)
    time.sleep(0.098)
    payment85 = get_payment_of_order(order85)
    etag85 = get_payment_status_etag(payment85)
    update_payment_status(payment85, 'payed', etag85, token)
    time.sleep(0.162)
    payment59 = get_payment_of_order(order59)
    etag59 = get_payment_status_etag(payment59)
    update_payment_status(payment59, 'payed', etag59, token)
    time.sleep(0.168)
    payment99 = get_payment_of_order(order99)
    etag99 = get_payment_status_etag(payment99)
    update_payment_status(payment99, 'payed', etag99, token)
    time.sleep(0.081)
    payment84 = get_payment_of_order(order84)
    etag84 = get_payment_status_etag(payment84)
    update_payment_status(payment84, 'payed', etag84, token)
    time.sleep(0.021)
    payment58 = get_payment_of_order(order58)
    etag58 = get_payment_status_etag(payment58)
    update_payment_status(payment58, 'payed', etag58, token)
    time.sleep(0.152)
    payment2 = get_payment_of_order(order2)
    etag2 = get_payment_status_etag(payment2)
    update_payment_status(payment2, 'payed', etag2, token)
    time.sleep(0.122)
