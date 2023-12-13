import sys

if len(sys.argv) == 1:
	print('Please use `python getdoc.py` n')
	sys.exit()
try:
	n = int(sys.argv[1])
except ValueError:
	print('Illegal use: n must be an integer')
	sys.exit()

with open('collection.tsv', 'r') as f:
	for _ in range(0, n):
		s = f.readline()
	print(f.readline())
	
	
