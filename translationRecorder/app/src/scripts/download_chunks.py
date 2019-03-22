""" Script to generate a json file containing book name, number of
    chapters, number of chunks """

import json
import urllib.request
import re
import os

ROOT_DIR = os.path.dirname(os.path.realpath(__file__))

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

    if slug == "obs":
        continue

    meta = DATA[x]["meta"][0]
    anth = OT if meta == "bible-ot" else NT

    chunks_url = BASE_URL + slug + "/" + EN + "/ulb/chunks.json"
    outpath = os.path.join(ROOT_DIR, "chunks", anth, slug, "chunks.json")
    os.makedirs(os.path.dirname(outpath), exist_ok=True)
    urllib.request.urlretrieve(chunks_url, outpath)
