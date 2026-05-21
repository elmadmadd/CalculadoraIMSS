p = 35000
pmt = 3561.07
n = 12

def calculate_rate(capital, payment, n):
    r = 0.03
    for _ in range(100):
        val = payment * (1 - (1+r)**-n)/r
        diff = val - capital
        dev = payment * (n*(1+r)**(-n-1)/r - (1 - (1+r)**-n)/(r**2))
        if dev == 0: break
        r -= diff/dev
    return r

r = calculate_rate(p, pmt, n)
saldo = p
for _ in range(9):
    intereses = saldo * (r)
    capital = pmt - intereses
    saldo -= capital

print(f"Saldo: {saldo}")
int_diario = saldo * r / 30.416666
print(f"A 23 dias (base 365/12): {saldo + int_diario * 23}")

# What if IVA is extracted first?
# 3561.07 is payment with IVA. 
# It's an unsolved problem exactly how they round.
# What if the monthly rate is computed as a nominal rate * 1.16?
