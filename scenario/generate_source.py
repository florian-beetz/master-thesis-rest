import random
from pprint import pprint

MAX_ITEMS = 4
MAX_POSITION_SIZE = 3
MAX_SLEEP = 250
ITEM_COUNT = 5
ORDER_COUNT = 100

if __name__ == "__main__":
    for i in range(1, ORDER_COUNT + 1):
        item_count = random.randint(1, MAX_ITEMS)
        items = random.sample(range(1, ITEM_COUNT + 1), item_count)
        positions = {item: random.randint(1, MAX_POSITION_SIZE) for item in items}
        print("    order{0} = create_order({{{1}}}, {0})".format(i, ', '.join(['item{0}: {1}'.format(order, amount) for order, amount in positions.items()])))
        print("    time.sleep({0})".format(random.randint(1, MAX_SLEEP) / 1000))

    print("    ")
    print("    time.sleep(120)")
    print("    ")

    r = list(range(1, ORDER_COUNT + 1))
    random.shuffle(r)
    for i in r:
        print("    payment{0} = get_payment_of_order(order{0})".format(i))
        print("    etag{0} = get_payment_status_etag(payment{0})".format(i))
        print("    update_payment_status(payment{0}, 'payed', etag{0}, token)".format(i))
        print("    time.sleep({0})".format(random.randint(1, MAX_SLEEP) / 1000))

