# PwndaHash
Plinko based encryption algorithm

Developers: Jaylen Hill & Dominick Rodriguez

Welcome to PwndaHash!

Purpose:

Proof-of-concept file encryption based off gambling game "Plinko" or Pachinko board game
Originally designed as a way to encrypt files to store crypto wallet mnemonic phrases with a singular memorable password without having to store a long key. 


Mechanics:

Password get turned into buckets at the bottom of the board. Each character in a file gets turned into a ball and then released. When a ball hits a bucket the two values get added together as well as a salt that contains the value of the last character value of the password itself. The sum along with the password index get stored together in a separate file. Despite balls having different falling rates, they are processed and written to an output file as if they were first in first out.


Security:

The password index and encrypted character are stored together in plain text.
The password index indicates location of the character that the "ball" hit. Knowing the location of the character in the password is useful when decrypting, but does not provide the value unless the password is known. Without prior knowledge it is impossible to determine which three values were added to create the encrypted character. Theoretically, when brute forcing a password the output of any attempted decryption is infinite due to the irreversibility of addition without prior knowledge. The encryption is based on normal distribution, so the "balls" are focused more towards the center of the password; however the salt is still required to decrypt the characters, and due to normal distribution the password length is not revealed. Theoretically, a user can have a 512 bit password but the output can only show 64-256 index length. Additionally, with PwndaHash being based on UTF-8 the user has the option to choose any character out of the UTF-8 codepoints from BMP to supplementary, roughly 1,112,064 codepoints. However, when only considering the range 32-128 codepoints for ASCII users there are 96 options for a password. A 10 character password is fairly easy to remember, and with 96 possibilities there are (96^10 = 6.65 * 10^19)  or 66 quintillion possibilities. This can be scaled with a larger range using the full extent of the utf-8 codepoints and extending the password length to 512 characters.

Weaknesses:

Brute forcing. If password is too weak or computer is strong Brute forcing is always possible (However unlikely)

Storage. PwndaHash tends to expand file sizes due to the way the encrypted values are stored. PwndaHash roughly doubles the original file size; however, to compensate the program can store the encrypted file in a zip folder and read from a zip folder to decrypt any PwndaHash encrypted files. This is not a major concern due to computer storage growing in size over time, and file compression being able to compress files up to 90% less than the original size


Strengths:

Irreversible. Encrypted values are hard to determine due to addition, without prior knowledge it is impossible to deduce which two or three numbers created a sum

Unrepeatable. Each new encryption output is different due to normal distribution even if the password is the same, so the same file will never have the same output even if they used the same password

Large Possibilities. Using utf-8 for passwords and as encrypted values creates really large possible password combinations, also brute forcing an encrypted character can create a infinite number of outputs with no certainty of the original message. The message "The red fox" can be encrypted and the brute forced output could be "wUe 3<w a&(" or "ran ice tea". 












Possible Handshake:
Public Key: Encrypted File
Private Key: Password


PC1 sends encrypted file using private key
PC1 requests a the unencrypted Version
PC2 decrypts the file using private and sends it back
PC1 Checks if decrypted version matches they're own
