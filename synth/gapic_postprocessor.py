import os
import sys

if __name__ == '__main__':
    os.environ['PYTHONNOUSERSITE'] = 'True'

    sys.argv[2] = os.path.abspath(sys.argv[2])
    sys.argv[3] = os.path.abspath(sys.argv[3])
    sys.argv[4] = os.path.abspath(sys.argv[4])
    os.chdir(sys.argv[1])

    args = [sys.executable] + sys.argv[2:]

    os.execv(args[0], args)
