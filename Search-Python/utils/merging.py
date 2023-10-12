import pickle

class MergeBloks:
    """
    This class read the blocks one by one, and the term's block one at a time.
    When we find a term that it's already present into the merging inverted index, just merge its posting list.
    At the end class save the complete inverted index in an output file, specified by the user
    """
    def __init__(self):
        self.outputfile = "inverted_index/inverted_index"
        self.block_num = 0

    def merging(self):
        pass
