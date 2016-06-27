#Script to generate a json file containing book name, number of chapters, number of chunks
import json
import urllib.request
#import re

response_obs = urllib.request.urlopen("https://api.unfoldingword.org/obs/txt/1/en/obs-en.json")
obs = json.loads(response_obs.read().decode('utf-8'))
num_chunks = []
for x in range(50):
	size = len(obs['chapters'][x]['frames'])
	num_chunks.append(size)

for x in num_chunks:
	print(x)

'''
result_json_name = "chunks.json"

with open("catalog.json") as file:
	data = json.load(file)

output = []

#skip obs for now, loop over all books
for x in range(1, 67):
	#gives book name and order (the books are stored out of order in the json)
	slug = data[x]["slug"]
	sort = data[x]["sort"]

	#Get languages.json
	url_lang_cat = data[x]["lang_catalog"]
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
	lines =	usfm_data.splitlines()

	#keep a count of \c and \s5 tags (chapter and chunk respectively)
	chapter = 0
	num_chunks = 0
	chunk_list = []
	for line in lines:
		chunk_match = re.search(r'\\s5', line)
		#add to the number of chunks seen so far
		if chunk_match:
			num_chunks += 1
		#on a new chapter, append the number of chunks tallied and reset the count
		chapter_match = re.search(r'\\c', line)
		if chapter_match:
			chunk_list.append(num_chunks)
			num_chunks = 0
			chapter += 1
	#append the last chapter
	chunk_list.append(num_chunks+1)
	#Account for the off by one introduced from chunks coming before chapters
	chunk_list_fixed = []
	length = len(chunk_list)-1
	#eliminate chapter "0"
	for i in range(length):
		chunk_list_fixed.append(chunk_list[i+1])

	#create a dictionary to store the book's data
	book = {}
	book['slug'] = slug
	book['name'] = name
	book['sort'] = sort
	book['chapters'] = len(chunk_list_fixed)
	book['chunks'] = chunk_list_fixed
	#add to the list of books
	output.append(book)

#output all book data to a json file
with open(result_json_name, 'w') as outfile:
	json.dump(output, outfile)

'''