""" Script to generate a json file containing book name, number of
    chapters, number of chunks """

import json
import urllib.request
import re
import os

ROOT_DIR = os.path.dirname(os.path.realpath(__file__))

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

def process_chapter(chap):
    processed_chap = []
    for key, value in chap.items():
        processed_chap.append({"id":key, "tn":value})
    return processed_chap

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

note_book_slugs = []

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
        outpath = os.path.join(ROOT_DIR, "chunks", "tn", slug + "-ch-" + str(chap), "chunks.json")
        tmp_slg = slug + "-ch-" + str(chap)
        note_book_slugs.append({"slug":tmp_slg, "anth":"tn", "num":((int(sort)*100)+int(chap)), "name": tmp_slg})
        os.makedirs(os.path.dirname(outpath), exist_ok=True)
        with(open(outpath,'w')) as outfile:
            json.dump(process_chapter(book[chap]), outfile)

with(open("note_books.json", 'w')) as outfile:
    json.dump(note_book_slugs, outfile)

print("done")
