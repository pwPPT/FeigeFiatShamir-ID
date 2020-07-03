from flask import Flask, jsonify, request
from random import randint

from ffs import Verifier

import json


app = Flask(__name__)

base_url = '/api/ca/'

N = 39769 * 50423
# pub_key = [1029024793, 349485908, 1492335079, 161892948, 1893287961, 498154583, 748209765, 1555624508]
# N = 1009 * 1019
# pub_key = [890606, 191646, 188005, 898857, 235664, 717573, 124925, 625063]

# STATE:
username = None
pub_key = None
verifier = None
X = None
tries = randint(4, 8)
positive_tries = 0

@app.route(base_url + 'token', methods=['POST'])
def token():
    data = json.loads(request.data)

    print(data)
    resp_body = {
        'token': 'UBUYYUHBI^&YHIUEI^@UDYUGKQ'
    }
    return jsonify(resp_body)

@app.route(base_url + 'X', methods=['POST'])
def x():
    data = json.loads(request.data)
    global X
    X = data['X']
    a = verifier.gen_a()
    print(f'X: {X}    A: {a}')
    resp_body = {
        'A': a
    }
    return jsonify(resp_body)

@app.route(base_url + 'Y', methods=['POST'])
def y():
    global positive_tries, tries
    data = json.loads(request.data)
    Y = data['Y']
    print(f'Y: {Y}  X: {X}')
    is_verified = verifier.verify_y(X, Y)
    if is_verified:
        positive_tries += 1
        print(f'IsVerified: {is_verified}   tries: {positive_tries}/{tries}')
        if positive_tries == tries:
            positive_tries = 0
            tries = randint(4, 8)

            resp_body = {
                'repeat': False,
                'is_authenticated': True,
                'session_id': '*^BTIB&^i67u6btutbT^'
            }
            return jsonify(resp_body)
        else:
            resp_body = {
                'repeat': True,
                'is_authenticated': False,
                'session_id': None
            }
            return jsonify(resp_body)
    else:
        print(f'IsVerified: {is_verified}   tries: {positive_tries}/{tries}')
        positive_tries = 0
        tries = randint(4, 8)
        resp_body = {
                'repeat': False,
                'is_authenticated': False,
                'session_id': None
            }
        return jsonify(resp_body)

@app.route(base_url + 'register', methods=['POST'])
def register():
    global username, pub_key, verifier
    data = json.loads(request.data)
    username = data['username']
    pub_key = data['public_key']
    verifier = Verifier(pub_key, N)

    print(f'Registered  ===  User: {username}   -   PubKey: {pub_key}')
    return jsonify({})
