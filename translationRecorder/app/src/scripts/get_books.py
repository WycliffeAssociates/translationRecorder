""" Script to generate a json file containing book names  """

import json
import urllib.request
import re

RESULT_JSON_NAME = "books.json"

# Get catalog.json
URL_CAT = "https://api.unfoldingword.org/uw/txt/2/catalog.json"
response_cat = urllib.request.urlopen(URL_CAT)
DATA = json.loads(response_cat.read().decode('utf-8'))

OUTPUT = []

LANGS = DATA["cat"][0]["langs"]

# just a pointer, since the full path is dynamic-ish
langIdx = []
for idx in range(len(LANGS)):
    if LANGS[idx]['lc'] == 'en':
        langIdx = LANGS[idx]

# skip obs for now, loop over all books
for x in range(0, 66):
    # gives book name and order (the books are stored out of order in the json)

    # Having to navigate through DATA a bit because the location of English may move as more languages are added
    # Using English because the list of books may not be complete in other languages
    # 0 index after cat is for Bible, 1 contains data for obs
    # 0 index after vers is for ULB, though UDB would also be fine for this case
    slug = langIdx["vers"][0]["toc"][x]["slug"]
    name = langIdx["vers"][0]["toc"][x]["title"]
    print(slug)
    # sort+1 so that 0 can be for OBS in the future
    number = x + 1
    # anthology designates what higher collection a book is a part of
    anthology = 'ot'
    # door43 convention skips number 40. Makes sense to change the sort to be book number
    if number > 39:
        number = number + 1
        anthology = 'nt'

    # create a dictionary to store the book's data
    book = {}
    book['slug'] = slug
    book['name'] = name
    book['num'] = number
    book['anth'] = anthology
    # add to the list of books
    OUTPUT.append(book)

# output all book data to a json file
with open(RESULT_JSON_NAME, 'w') as outfile:
    json.dump(OUTPUT, outfile)
