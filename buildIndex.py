# --------------------PACKAGES--------------------
from icecream import ic
from typing import List, Tuple
import sys

# ----------------GLOBAL VARIABLES----------------
# TODO -> Pick those from XML or input parameters

CORPUS_PATH = "/data"


# --------------MAIN-ONLY FUNCTIONS---------------
def getparams(lst: List[str]) -> Tuple[bool, str]:
    # TODO -> By now, debug mode is set to true
    debug = True
    mode = "TFIDF"
    # TODO -> Inspect lst and get info about index mode and debug active/inactive
    return debug, mode


# ----------------STARTING POINT------------------
dbg, _ = getparams(sys.argv)

if not dbg: ic.disable()

ic()