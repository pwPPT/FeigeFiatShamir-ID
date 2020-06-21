from random import randint, randrange
from functools import reduce

# p = 39769, q = 50423,     N = 2005272287
# https://medium.com/asecuritysite-when-bob-met-alice/feige-fiat-shamir-and-zero-knowledge-proof-cdd2a972237c
# https://en.wikipedia.org/wiki/Feige%E2%80%93Fiat%E2%80%93Shamir_identification_scheme

class Prover:
    """
    Android app
    """

    def __init__(self, secret, N):
        """
        secret - bytes[], len(secret) = k
        N - integer = prime1 * prime2
        """
        self._secret = secret
        self.N = N
        self._r = None

    def gen_x(self):
        r = randint(1, 39768)
        self._r = r
        x = (r**2) % self.N
        return x
    
    def compute_y(self, a):
        # y = (r * ((s_1 ** a_1) * (s_2 ** a_2) * ... * (s_k ** a_k))) % N
        values = [s**i for (s, i) in zip(self._secret, a)]
        y = (self._r * reduce(lambda i, j: i * j, values)) % self.N
        return y

    def compute_public_key(self):
        """
        Compute list of s^2 % N for each s in secret
        """
        v = [(s**2) % self.N for s in self._secret]
        return v

    pass


class Verifier:
    """
    Web service
    """
    def __init__(self, public_key, N):
        self.public_key = public_key  # Peggy's public key
        self.N = N  # Shared N value
        self._a = []  # Generated {0, 1} values vector  

    def gen_a(self):
        """
        Generate k values from {0, 1} (k = len(secret))
        """
        a = [randint(0, 1) for _ in range(len(self.public_key))]
        self._a = a
        return a
        
    def verify_y(self, x, y):
        values = [s**i for (s, i) in zip(self.public_key, self._a)]
        y1 = (x * reduce(lambda i, j: i * j, values)) % self.N
        return y1 == (y ** 2) % self.N

def gen_rand_secret(size):
    return bytes([randint(0, 255) for _ in range(size)])
    
if __name__ == '__main__':
    p = 39769
    q = 50423
    N = p * q
    secret = gen_rand_secret(10)

    # Prover has his secret value and some N = prime1 * prime2
    prover = Prover(secret, N)
    public_key = prover.compute_public_key()
    # Verifier knows prover's public key and the same N value 
    verifier = Verifier(public_key, N)
    # Next steps should be repeated i times
    results = []
    i = 10
    for _ in range(i):
        # Verifier chooses vector 'a' of len(secret) or len(public_key) with {0, 1} values
        a = verifier.gen_a()
        # Prover sends generated x, and computed y values
        x = prover.gen_x()
        y = prover.compute_y(a)
        # Verification step
        is_verified = verifier.verify_y(x, y)
        results.append(is_verified)
    
    print(sum(results) == i)  # all values are True
