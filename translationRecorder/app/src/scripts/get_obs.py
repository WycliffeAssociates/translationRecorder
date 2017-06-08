#!/usr/bin/python
# -*- coding: utf-8 -*-
# Script to generate a json file containing book name, number of chapters, number of chunks

import json
import urllib.request

# import re

# {"firstvs": "27", "id": "18-27", "lastvs": "29"}

# {"slug": "gen", "num": 1, "anth": "ot", "name": "Genesis"}

response_obs = \
    urllib.request.urlopen('https://api.unfoldingword.org/obs/txt/1/en/obs-en.json'
                           )
obs = json.loads(response_obs.read().decode('utf-8'))
num_chunks = []
books = []
for x in range(50):
    size = len(obs['chapters'][x]['frames'])
    chunks = []
    for i in range(size):
        chunk = {}
        chunk['firstvs'] = str(x + 1)
		print(obs['chapters'][x]['frames']['id'])
        chunk['id'] = obs['chapters'][x]['frames']['id']
        chunk['lastvs'] = str(x + 1)
        chunks.append(chunk)
        num_chunks.append(size)
    with open(str(x + 1) + '/chunks.json', 'w') as outfile:
        json.dump(chunks, outfile)
    book = {}
    book['slug'] = str(x + 1)
    book['num'] = x + 1
    book['anth'] = 'obs'
    book['name'] = obs['chapters'][x]['title']
    books.append(book)

with open(str(x + 1) + '/chunks.json', 'w') as outfile:
    json.dump(books, outfile)