from decimal import Decimal, ROUND_HALF_UP

def to_money(val):
    try:
        if val is None: return "$0.00"
        if not isinstance(val, Decimal):
            # Limpiar string si viene con formato de moneda
            s_val = str(val).replace("$", "").replace(",", "").strip()
            if not s_val: return "$0.00"
            val = Decimal(s_val)
        rounded = val.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        return f"${rounded:,.2f}"
    except Exception:
        # En caso de cualquier error (valores infinitos, NaN, etc)
        return "$0.00"

def clean_float(v):
    try: return float(str(v).replace("$","").replace(",","").strip())
    except: return 0.0

def clean_decimal(v):
    try:
        s = str(v).replace("$","").replace(",","").strip()
        return Decimal(s) if s else Decimal("0")
    except:
        return Decimal("0")
