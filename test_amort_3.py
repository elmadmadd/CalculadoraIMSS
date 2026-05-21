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

print(f"Saldo despues de 9 pagos: {saldo}")

for j in range(15, 30):
    interes_diario = saldo * (r / j)
    # What if they just use 360 base? iAnual = r*12. Diario = iAnual / 360
    # Interest + IVA = ? No, r already includes IVA.
    int_acc = saldo * (r * 12 / 360) * j
    print(f"A {j} dias (base 360), a pagar: {saldo + int_acc}")

print("=== LET'S TRY DIFFERENT DAYS (What if last payment was NOT 01/03?) ===")
# "Ultima Fecha de Pago Aplicada 01/03/2026"
# Date is 24/03/2026. This is 23 days later.
