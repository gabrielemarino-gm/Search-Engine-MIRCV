# --------------------PACKAGES--------------------
from icecream import ic
from typing import List, Tuple

from utils import *

import sys

# ----------------GLOBAL VARIABLES----------------
# TODO -> Pick those from XML or input parameters

CORPUS_PATH = "/data"


# --------------MAIN-ONLY FUNCTIONS---------------
def getparams(lst: List[str]) -> Tuple[bool, str]:
    debug = True
    mode = "TFIDF"
    for opt in lst[1:]:
        ic(opt)
    # TODO -> Inspect lst and get info about index mode and debug active/inactive
    return debug, mode


# ----------------STARTING POINT------------------
dbg, _ = getparams(sys.argv)

if not dbg: ic.disable()

# Preprocessing poi SPIMI (Documento per documento) - Preprocessing dentro SPIMI oppure Preprocessing prima di darlo a SPIMI?
# Inverse Index Creation
#   -   Inverse Index Structure
#   -   Lexicon (Terms info)
#   -   Document Table (Document info [metadati in general])



# 1. Come fare Hash Table in Python
# 2. Come si controlla la memoria libera/occupata (RAM)

#TODO -> Modulo Preprocessing
#TODO -> Modulo SPIMI

# Inizializzare un preprocesser (come fare preprocessing)
# Abilitiamo lo stemming? Abilitiamo lo stopWord removing?

# 32    Ciao Gabri, io sono matteo
# {32, []}
# [{32, "ciao"},{32, "gabri"},{32, "io"},{32, "sono"},{32, "matteo"}]

#import psutil

# Ottieni le informazioni sull'uso della memoria RAM
#mem = psutil.virtual_memory()

# Stampa le informazioni sull'uso della memoria RAM
# print(f"Memoria totale: {mem.total} bytes")
# print(f"Memoria disponibile: {mem.available / (1024 ** 2)} megabytes")
# print(f"Memoria in uso: {mem.used / (1024 ** 3)} gigabytes")
# print(f"Percentuale di utilizzo della memoria: {mem.percent}%")



# ic(psutil.virtual_memory().percent)


# Path inside the main directory of the project (Search-Engine-MIRCV)
filepath_test = "../collection.tsv"
output_test = "../data/test/"


spimi = SPIMI(filepath_test, "cazzo", 4, True, True, True)
spimi.algorithm()