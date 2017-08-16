""" Script to generate a json file containing book name, number of
    chapters, number of chunks """

import json
import urllib.request
import re
import os

def process_notes_json(stuff):
    book = {}
    for chunk in stuff:
        if "id" not in chunk:
            continue
        chap = chunk["id"].split('-')[0]
        chk = chunk["id"].split('-')[1]
        if "tn" not in chunk or len(chunk["tn"]) < 1:
            continue
        if int(chap) not in book:
            book[int(chap)] = {}
        book[int(chap)][chk] = (chunk["tn"])
    return book

RESULT_JSON_NAME = "chunks/"

# with open("catalog.json") as file:
#     DATA = json.load(file)

# Get catalog.json
URL_CAT = "https://api.unfoldingword.org/ts/txt/2/catalog.json"
response_cat = urllib.request.urlopen(URL_CAT)
DATA = json.loads(response_cat.read().decode('utf-8'))
BASE_URL = "https://api.unfoldingword.org/ts/txt/2/"
EN = "en"
OT = "ot"
NT = "nt"
RES = ["ulb"]

OUTPUT = []

# skip obs for now, loop over all books
for x in range(0, 67):
    # gives book name and order (the books are stored out of order in the json)
    slug = DATA[x]["slug"]
    sort = DATA[x]["sort"]

    if slug == "obs":
        continue

    anth = OT if int(sort) < 41 else NT

    chunks_url = BASE_URL + slug + "/" + EN + "/notes.json"
    NOTE = json.loads(urllib.request.urlopen(chunks_url).read().decode('utf-8'))
    book = process_notes_json(NOTE)
    for chap in book:
        outpath = "notes/" + anth + "/" + slug + "-ch-" + str(chap) + "/chunks.json"
        os.makedirs(os.path.dirname(outpath))
        with(open(outpath,'w')) as outfile:
            json.dump(book[chap], outfile)
