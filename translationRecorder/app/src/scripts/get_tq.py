""" Script to generate a json file containing book name, number of
    chapters, number of chunks """

import json
import urllib.request
import re
import os

ROOT_DIR = os.path.dirname(os.path.realpath(__file__))

def process_questions_json(stuff):
    book = {}
    for chunk in stuff:
        if "id" not in chunk:
            continue
        chap = chunk["id"]
        if "cq" not in chunk or len(chunk["cq"]) < 1:
            continue
        if int(chap) not in book:
            book[int(chap)] = {}
        book[int(chap)] = (chunk["cq"])
    return book

def process_chapter(chap):
    processed_chap = []
    for key, value in chap.items():
        processed_chap.append({"id":key, "cq":value})
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

question_book_slugs = []

# skip obs for now, loop over all books
for x in range(0, 67):
    # gives book name and order (the books are stored out of order in the json)
    slug = DATA[x]["slug"]
    sort = DATA[x]["sort"]

    if slug == "obs":
        continue

    anth = OT if int(sort) < 41 else NT

    chunks_url = BASE_URL + slug + "/" + EN + "/questions.json"
    QUESTION = json.loads(urllib.request.urlopen(chunks_url).read().decode('utf-8'))
    
    book = process_questions_json(QUESTION)
    #for chap in book:
    outpath = os.path.join(ROOT_DIR, "chunks", "tq", "tq-" + slug, "chunks.json")
    os.makedirs(os.path.dirname(outpath), exist_ok=True)
    with(open(outpath, 'w')) as outfile:
        json.dump(book, outfile)

print("done")
