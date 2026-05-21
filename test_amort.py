import math

def calculate_rate(capital, payment, n):
    r = 0.03
    for _ in range(100):
        val = payment * (1 - (1+r)**-n)/r
        diff = val - capital
        # derivative
        dev = payment * (n*(1+r)**(-n-1)/r - (1 - (1+r)**-n)/(r**2))
        if dev == 0: break
        r -= diff/dev
    return r

p = 35000
pmt = 3561.07
n = 12
r = calculate_rate(p, pmt, n)
print(f"Mensual rate: {r}")

payments_made = 9
remaining = n - payments_made

saldo_corte = pmt * (1 - (1+r)**-remaining)/r
print(f"Saldo al corte (9 pagos): {saldo_corte}")

dias = 23 # 1 de marzo al 24 de marzo
saldo_con_dias_30 = saldo_corte + (saldo_corte * (r/30) * dias)
print(f"Saldo + 23 dias (r/30): {saldo_con_dias_30}")

r_anual = r * 12
saldo_con_dias_360 = saldo_corte + (saldo_corte * (r_anual/360) * dias)
print(f"Saldo + 23 dias (base 360): {saldo_con_dias_360}")

r_anual_nom = r * 12
saldo_con_dias_365 = saldo_corte + (saldo_corte * (r_anual_nom/365) * dias)
print(f"Saldo + 23 dias (base 365): {saldo_con_dias_365}")

