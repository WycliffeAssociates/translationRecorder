""" Script to generate a json file containing book names  """

import json
import urllib.request
import re

RESULT_JSON_NAME = "books.json"

#Get catalog.json
URL_CAT = "https://api.unfoldingword.org/uw/txt/2/catalog.json"
response_cat = urllib.request.urlopen(URL_CAT)
DATA = json.loads(response_cat.read().decode('utf-8'))

OUTPUT = []

#skip obs for now, loop over all books
for x in range(0, 66):
    #gives book name and order (the books are stored out of order in the json)
    slug = DATA["cat"][0]["langs"][1]["vers"][0]["toc"][x]["slug"]
    name = DATA["cat"][0]["langs"][1]["vers"][0]["toc"][x]["title"]
    #sort+1 so that 0 can be for OBS in the future
    sort = x+1

    #create a dictionary to store the book's data
    book = {}
    book['slug'] = slug
    book['name'] = name
    book['sort'] = sort
    #add to the list of books
    OUTPUT.append(book)

#output all book data to a json file
with open(RESULT_JSON_NAME, 'w') as outfile:
    json.dump(OUTPUT, outfile)
