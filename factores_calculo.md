# Factores de Cálculo Exactos - ConsuCalcu

Este documento detalla todos los factores de cálculo financieros, límites mínimos de monto, tasas de interés mensuales y factores de seguro exactos configurados y utilizados en la aplicación **ConsuCalcu** para cada cotizador y plazo disponible.

---

## 1. Factores de Cálculo Financiero y Montos Mínimos

Estos factores se extraen de [FinancialConfig.kt](file:///Users/madd/AndroidStudioProjects/consucalcu/app/src/main/java/com/example/consucalcu/data/FinancialConfig.kt) y son utilizados en [FinancialEngine.kt](file:///Users/madd/AndroidStudioProjects/consucalcu/app/src/main/java/com/example/consucalcu/features/FinancialEngine.kt) para calcular el pago mensual o el monto del crédito.

### Fórmulas de Pago Mensual
1. **Modalidad Tradicional (Por defecto):**
   $$\text{Pago Mensual} = \frac{\text{Monto Solicitado}}{1000} \times \text{Factor Tradicional}$$
   *(Ajuste excepcional: En `CONSUPAGO ESPECIAL` a 60 meses con un monto exacto de $250,000, el pago se ajusta manualmente a $8,314.31 en lugar de $8,314.32).*

2. **Modalidad Retención (Sipre Global - Plazo 60):**
   Aplica únicamente para el plazo de 60 meses cuando se selecciona "Usar Retención". Utiliza un algoritmo iterativo donde:
   $$\text{Pago Mensual} = \frac{\text{Monto Base} \times \text{Factor de Retención}}{60}$$
   *(Redondeado a 2 decimales).*

---

### Tablas de Factores Financieros por Cotizador

#### CONSUBANCO

| Producto | Plazo | Factor Tradicional | Factor Retención | Mínimo por Plazo | Tasa Ref | CAT Trad | CAT Ret |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **CONSUBANCO TRADICIONAL** | 60 | `34.47786` | `30.95418` | $50,000 | 2.50% | 39.03% | 31.50% |
| | 54 | `36.01578` | N/A | $75,000 | | | |
| | 48 | `38.34620` | N/A | $100,000 | | | |
| | 36 | `45.12133` | N/A | $100,000 | | | |
| **CONSUBANCO ESPECIAL** | 60 | `31.62828` | N/A | $100,000 | 2.50% | 32.94% | 31.50% |
| **CONSUBANCO VIP** | 60 | `30.95419` | N/A | $150,000 | 2.50% | 39.03% | 31.50% |

* **Monto Mínimo Global:** Tradicional ($50,000), Especial ($100,000), VIP ($150,000)
* **Incremento Mínimo (Múltiplo):** $1,000
* **Regla de Retención:** Monto mínimo de $100,000.

---

#### CONSUPAGO

| Producto | Plazo | Factor Tradicional | Factor Retención | Mínimo por Plazo | Tasa Ref | CAT Trad | CAT Ret |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **CONSUPAGO TRADICIONAL** | 60 | `36.61922` | `33.25726` | $5,000 | 2.73% | 43.62% | 36.42% |
| | 54 | `38.09840` | N/A | $5,000 | | | |
| | 48 | `40.07602` | N/A | $10,000 | | | |
| | 36 | `46.26983` | N/A | $15,000 | | | |
| | 24 | `59.51904` | N/A | $25,000 | | | |
| | 12 | `100.90633` | N/A | $25,000 | | | |
| **CONSUPAGO ESPECIAL** | 60 | `33.25726` | N/A | $50,000 | 2.73% | 36.42% | 36.42% |

* **Monto Mínimo Global:** Tradicional ($5,000), Especial ($50,000)
* **Incremento Mínimo (Múltiplo):** $500
* **Regla de Retención:** Monto mínimo de $50,000.

---

#### OPCIPRES

| Producto | Plazo | Factor Tradicional | Factor Retención | Mínimo por Plazo | Tasa Ref | CAT Trad | CAT Ret |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **OPCIPRES TRADICIONAL** | 60 | `35.98563` | `32.22284` | $30,000 | 2.67% | 42.26% | 34.21% |
| | 54 | `37.65908` | N/A | $45,000 | | | |
| | 48 | `39.86519` | N/A | $50,000 | | | |
| | 36 | `46.31088` | N/A | $70,000 | | | |
| **OPCIPRES ESPECIAL** | 60 | `32.22284` | N/A | $75,000 | 2.67% | 34.21% | 34.21% |

* **Monto Mínimo Global:** Tradicional ($30,000), Especial ($75,000)
* **Incremento Mínimo (Múltiplo):** $750
* **Regla de Retención:** Monto mínimo de $75,000.

---

#### MÁS NÓMINA (MSN)

| Producto | Plazo | Factor Tradicional | Factor Retención | Mínimo por Plazo | Tasa Ref | CAT Trad | CAT Ret |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **MASNOMINA** | 60 | `36.777479` | N/A | $10,000 | 2.73% | 43.96% | 46.50% |
| | 54 | `36.01578` | N/A | $75,000 | | | |
| | 48 | `40.18578` | N/A | $15,000 | | | |
| | 36 | `46.37244` | N/A | $15,000 | | | |
| | 24 | `59.57626` | N/A | $20,000 | | | |

* **Monto Mínimo Global:** $10,000
* **Incremento Mínimo (Múltiplo):** $0
* **Regla de Retención:** N/A (Deshabilitado: monto mínimo configurado en $999,999,999).

---

## 2. Tasas de Interés y Factores de Seguro IMSS

Estos valores provienen de [NormativaIMSS.kt](file:///Users/madd/AndroidStudioProjects/consucalcu/app/src/main/java/com/example/consucalcu/data/NormativaIMSS.kt). La tasa de interés mensual y el factor de seguro mensual se determinan con base en el producto y plazo para la amortización del seguro.

### Tasa de Interés Mensual (%)

| Producto / Cotizador | 12 meses | 24 meses | 36 meses | 48 meses | 54 meses | 60 meses |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **CONSUBANCO TRADICIONAL** | 2.40% | 2.40% | 2.50% | 2.44% | 2.40% | 2.40% |
| **CONSUBANCO ESPECIAL** | - | - | - | - | - | 2.07% |
| **CONSUBANCO VIP** | - | - | - | - | - | 1.99% |
| **CONSUPAGO TRADICIONAL** | 2.65% | 2.65% | 2.65% | 2.65% | 2.65% | 2.65% |
| **CONSUPAGO ESPECIAL** | - | - | - | - | - | 2.26% |
| **OPCIPRES TRADICIONAL** | 2.57% | 2.57% | 2.65% | 2.62% | 2.59% | 2.57% |
| **OPCIPRES ESPECIAL** | - | - | - | - | - | 2.14% |
| **MASNOMINA** | 2.66% | 2.66% | 2.66% | 2.66% | 2.66% | 2.66% |

---

### Factor de Seguro Mensual
Se cobra mensualmente sobre el **saldo asegurable** (Saldo Inicial + Intereses del periodo + IVA):
$$\text{Seguro Mensual} = \frac{\text{Saldo Asegurable} \times \text{Factor de Seguro}}{1000}$$

* **OPCIPRES ESPECIAL:** `1.99`
* **OPCIPRES TRADICIONAL:** `1.8304`
* **CONSUBANCO (Todos):** `1.7571`
* **CONSUPAGO (Todos):** `1.7571`
* **MASNOMINA (Todos):** `1.7571`

---

## 3. Factores de Seguro Estáticos (Ajuste para Match exacto con Excel)

Para **OPCIPRES, CONSUPAGO y MASNOMINA**, el costo total del seguro o el costo del primer año se calcula multiplicando directamente el monto por un factor estático (definido en `FinancialConfig.INSURANCE_FACTORS` y aplicado en [CalculatorViewModel.kt](file:///Users/madd/AndroidStudioProjects/consucalcu/app/src/main/java/com/example/consucalcu/viewmodel/CalculatorViewModel.kt)):
$$\text{Costo del Seguro} = \text{Monto Solicitado} \times \text{Factor Estático}$$

Los factores se definen como `Pair(Factor Primer Año, Factor Plazo Completo)`:

| Producto / Proveedor | Plazo | Factor Primer Año (12 meses) | Factor Plazo Completo |
| :--- | :---: | :---: | :---: |
| **CONSUPAGO** | 12 | `0.01242425` | `0.01242425` |
| | 24 | `0.01791520` | `0.02524355` |
| | 36 | `0.01966445` | `0.03929660` |
| | 48 | `0.02048600` | `0.05452267` |
| | 54 | `0.02074000` | `0.06251715` |
| | 60 *(Tradicional)* | `0.02093620` | `0.07078560` |
| | 60 *(Retención - Key 999)* | `0.02346925` | `0.07753355` |
| **OPCIPRES** | 36 | `0.02048800` | `0.04094900` |
| | 48 | `0.02132520` | `0.05668885` |
| | 54 | `0.02157555` | `0.06486105` |
| | 60 *(Tradicional)* | `0.02176960` | `0.07329780` |
| | 60 *(Retención - Key 999)* | `0.02338955` | `0.07668515` |
| **MASNOMINA** | 24 | `0.01791990` | `0.02525320` |
| | 36 | `0.01967240` | `0.03932890` |
| | 48 | `0.02049400` | `0.05457600` |
| | 54 | `0.02333205` | `0.06941680` |
| | 60 | `0.02094585` | `0.07089285` |

*(Nota: En CONSUBANCO, aunque el seguro se calcula mediante amortización mensual, se aplican ajustes de redondeo fino para coincidir con Excel: a 54 meses el Costo Total se fija en `$12,258.52` si resulta `$12,258.51`, y a 48 meses se fija en `$10,733.60` si resulta `$10,733.59`).*
