# --------------------PACKAGES--------------------
from icecream import ic
from typing import List, Tuple, Dict
import sys
import time

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


# Preprocessing poi SPIMI (Documento per documento) - Preprocessing dentro SPIMI oppure Preprocessing prima di darlo a SPIMI?
# Inverse Index Creation
#   -   Inverse Index Structure
#   -   Lexicon (Terms info)
#   -   Document Table (Document info [metadati in general])

# 2. Come si controlla la memoria libera/occupata (RAM)

#import psutil

# Ottieni le informazioni sull'uso della memoria RAM
#mem = psutil.virtual_memory()

# Stampa le informazioni sull'uso della memoria RAM
# print(f"Memoria totale: {mem.total} bytes")
# print(f"Memoria disponibile: {mem.available / (1024 ** 2)} megabytes")
# print(f"Memoria in uso: {mem.used / (1024 ** 3)} gigabytes")
# print(f"Percentuale di utilizzo della memoria: {mem.percent}%")


# Path inside the main directory of the project (Search-Engine-MIRCV)
# collection_path = "./data/collection.tsv"
# outputfolder_path = "./data/test"
#
# spimi = SPIMI(collection_path, outputfolder_path, 75, True, True, True)
# spimi.algorithm()


