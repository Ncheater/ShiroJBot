# -*- coding: utf-8 -*-
from cryptography.fernet import Fernet
import hashlib
import os.path
import sys

def encrypt(t, k):
    f = Fernet(k)
    fb = t.encode("UTF-8")
    return f.encrypt(fb)

encFile = encrypt(sys.argv[0], sys.argv[1]).decode("UTF-8")
hashFile = hashlib.md5(encFile).hexdigest()