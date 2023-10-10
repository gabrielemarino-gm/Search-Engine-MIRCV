# --------------------PACKAGES--------------------
from icecream import ic
from typing import List, Tuple, Dict
import sys
import time
from utils import Preprocesser, SPIMI
from structure import Vocabulary, InvertedIndex


# ----------------GLOBAL VARIABLES----------------
# TODO -> Pick those from XML or input parameters

CORPUS_PATH = "/data"


# --------------MAIN-ONLY FUNCTIONS---------------
def getparams(lst: List[str]) -> Tuple[bool, str]:
    mod = 'TFIDF'
    dbg = False
    for i in range(1, len(lst)):
        if lst[i] == '-d':
            dbg = True
        elif lst[i] == '--mode':
            mod = lst[i+1]
    return dbg, mod


# ----------------STARTING POINT------------------
DEBUG, MODE = getparams(sys.argv)

if not DEBUG: ic.disable()

# Path inside the main directory of the project (Search-Engine-MIRCV)
collection_path = "../mini_collection.tsv"
outputfolder_path = "Data"

spimi = SPIMI(collection_path, outputfolder_path, 75, True)
spimi.algorithm(debug=True)


#def get_next_doc() -> str:
#    with open('./data/redux.tsv', 'r', encoding='utf-8') as f:
#        for line in f:
#            yield line


#processor = Preprocesser(True)
# TODO -> Times on 100k documents
# TODO -> BASE = ~5 seconds
# TODO -> BASE + URL = ~6 seconds
# TODO -> BASE + STOPWORDS = ~37 seconds
# TODO -> BASE + STEMMING = ~90 seconds
# TODO -> BASE + URL + STEMMING + STOPWORD = ~93 seconds

#temp = []
#
#start = time.time_ns()
#for l in get_next_doc():
#    temp.append(processor.process(l)[1])
#end = time.time_ns()
#
#ic(f'Time spent in Preprocessing: {(end-start)/10**9} seconds')
#
#voc = Vocabulary()
#iidx = InvertedIndex()
#
#start = time.time_ns()
#for i in range(len(temp)):
#    for term in temp[i]:
#        voc.add(term)
#        iidx.add(i, term)
#        pass
#end = time.time_ns()

# TODO -> Indexing of 100k documents with stopwords (more tokens)
# TODO -> WITH STOPWORDS = ~12 seconds
# TODO -> WITHOUT STOPWORDS = ~7 seconds

#ic(f'Time spent in Indexing: {(end-start)/10**9} seconds')
