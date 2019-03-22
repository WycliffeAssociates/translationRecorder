#!/usr/bin/python
# -*- coding: utf-8 -*-
# Script to generate a json file containing book name, number of chapters, number of chunks

import json
import urllib.request
import os

ROOT_DIR = os.path.dirname(os.path.realpath(__file__))

# import re
# {"firstvs": "27", "id": "18-27", "lastvs": "29"}
# {"slug": "gen", "num": 1, "anth": "ot", "name": "Genesis"}

RESPONSE_OBS = urllib.request.urlopen(
    'https://api.unfoldingword.org/obs/txt/1/en/obs-en.json')
obs = json.loads(RESPONSE_OBS.read().decode('utf-8'))
num_chunks = []
books = []

root_path = os.path.join(ROOT_DIR, "chunks/obs")

for x in range(50):
    size = len(obs['chapters'][x]['frames'])
    chunks = []
    for i in range(size):
        chunk = {}
        chunk['firstvs'] = str(i + 1)
        chunk['id'] = "01-{num:02d}".format(num=(i+1))
        chunk['lastvs'] = str(i + 1)
        chunks.append(chunk)
        num_chunks.append(size)
    os.makedirs(os.path.join(root_path, str(x + 1)), exist_ok=True)
    with open(os.path.join(root_path, str(x + 1), 'chunks.json'), 'w') as outfile:
        json.dump(chunks, outfile)
    book = {}
    book['slug'] = str(x + 1)
    book['num'] = x + 1
    book['anth'] = 'obs'
    book['name'] = obs['chapters'][x]['title']
    books.append(book)
try:
    with open('books.json', 'w') as outfile:
        json.dump(books, outfile)
except FileNotFoundError:
    print("error")
