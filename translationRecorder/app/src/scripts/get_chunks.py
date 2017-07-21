""" Script to generate a json file containing book name, number of
    chapters, number of chunks """

import json
import urllib.request
import re

RESULT_JSON_NAME = "chunks.json"

# with open("catalog.json") as file:
#     DATA = json.load(file)

# Get catalog.json
URL_CAT = "https://api.unfoldingword.org/ts/txt/2/catalog.json"
response_cat = urllib.request.urlopen(URL_CAT)
DATA = json.loads(response_cat.read().decode('utf-8'))

OUTPUT = []

#skip obs for now, loop over all books
for x in range(1, 67):
    #gives book name and order (the books are stored out of order in the json)
    slug = DATA[x]["slug"]
    sort = DATA[x]["sort"]

    #Get languages.json
    url_lang_cat = DATA[x]["lang_catalog"]
    response_lang_cat = urllib.request.urlopen(url_lang_cat)
    lang_catalog = json.loads(response_lang_cat.read().decode('utf-8'))

    name = lang_catalog[0]["project"]["name"]

    #Get resources.json
    #0 is for udb, are chunks the same for both?
    url_res = lang_catalog[0]["res_catalog"]
    response_res = urllib.request.urlopen(url_res)
    res_cat = json.loads(response_res.read().decode('utf-8'))

    #Get the usfm file
    url_usfm = res_cat[0]["usfm"]
    response_usfm = urllib.request.urlopen(url_usfm)
    usfm_data = response_usfm.read().decode('utf-8')
    lines = usfm_data.splitlines()

    def create_chunk(chapter_id, chunk_id, start_verse, end_verse):
        """ Create a dictionary of chunk data """
        chunk_data = {
            "chapter_id": chapter_id,
            "chunk_id": chunk_id,
            "start_verse": start_verse,
            "end_verse": end_verse}
        return chunk_data


    #keep a count of \c and \s5 tags (chapter and chunk respectively)
    chapter = 0
    num_chunks = 0
    chapters_in_book = []
    chunks_in_chapter = []
    current_chunk_start_verse = 1
    current_verse = 1
    for line in lines:
        verse_match = re.search(r'^\\v (\d+) ', line)
        if verse_match:
            current_verse = int(verse_match.group(1))
        chunk_match = re.search(r'\\s5', line)
        #add to the number of chunks seen so far
        if chunk_match:
            num_chunks += 1
            chunks_in_chapter.append(create_chunk(
                chapter, num_chunks, current_chunk_start_verse, current_verse))
            current_chunk_start_verse = current_verse + 1
        #on a new chapter, append the number of chunks tallied and reset the count
        chapter_match = re.search(r'\\c', line)
        if chapter_match:
            chapters_in_book.append(chunks_in_chapter)
            num_chunks = 0
            chapter += 1
            current_verse = 1
            current_chunk_start_verse = 1
            chunks_in_chapter = []

    #append the last chunk
    num_chunks += 1
    chunks_in_chapter.append(create_chunk(
        chapter, num_chunks, current_chunk_start_verse, current_verse))
    #append the last chapter
    chapters_in_book.append(chunks_in_chapter)
    #Account for the off by one introduced from chunks coming before chapters
    chunk_list_fixed = []
    length = len(chapters_in_book)-1
    #eliminate chapter "0"
    for i in range(length):
        chunk_list_fixed.append(chapters_in_book[i+1])

    #create a dictionary to store the book's data
    book = {}
    book['slug'] = slug
    book['name'] = name
    book['sort'] = sort
    book['chapters'] = len(chunk_list_fixed)
    book['chunks'] = chunk_list_fixed
    #add to the list of books
    OUTPUT.append(book)

#output all book data to a json file
with open(RESULT_JSON_NAME, 'w') as outfile:
    json.dump(OUTPUT, outfile)
