import math

def calculate_rate(capital, payment, n):
    r = 0.03
    for _ in range(100):
        val = payment * (1 - (1+r)**-n)/r
        diff = val - capital
        dev = payment * (n*(1+r)**(-n-1)/r - (1 - (1+r)**-n)/(r**2))
        if dev == 0: break
        r -= diff/dev
    return r

p = 35000
pmt = 3561.07
n = 12

print("=== Asumiendo 12 QUINCENAS ===")
r_q = calculate_rate(p, pmt, n)
print(f"Quincenal rate: {r_q}")

payments_made = 9
remaining = n - payments_made

saldo_corte = pmt * (1 - (1+r_q)**-remaining)/r_q
print(f"Saldo al corte (9 pagos): {saldo_corte}")

# Dias: 01/03/2026 to 24/03/2026 = 23 days.
# Int rate diely: r_q / 15.2
dias = 23
saldo_con_dias_15 = saldo_corte + (saldo_corte * (r_q/15.2) * dias)
print(f"Saldo + 23 dias (base 15.2): {saldo_con_dias_15}")

saldo_con_dias_360 = saldo_corte + (saldo_corte * (r_q*24/360) * dias)
print(f"Saldo + 23 dias (base 360 anual): {saldo_con_dias_360}")

print("=== SI FUERAN 24 QUINCENAS? (pagos = 24, pero dice 12) ===")

