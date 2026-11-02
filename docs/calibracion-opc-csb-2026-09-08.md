# Calibración OPC y CSB — 2026-09-08

## Ampliación con el Excel de comparaciones

`ConsuCalcu_Banco_Comparaciones_2026-09-08 (2).xlsx` contiene 32 casos
distintos de OPC/CSB a 60 meses. Confirma los factores de OPC, OPC MAX,
OPC ESP, CSB y CSB ESP. CSB VIP se ajusta de 30.95419 a **30.9541845**,
dentro de la intersección compatible [30.9541833333333, 30.9541857142857).
Así $350,000 produce $10,833.96, no $10,833.97. No se redondea el factor
a cinco decimales antes de calcular.

`ExcelQuoteRegressionTest` conserva los 32 casos y sus totales de referencia.
Comprueba la fórmula en todos; en el motor comprueba los 31 elegibles y
el rechazo de CSB VIP por $100,000, inferior al mínimo vigente de $150,000.
Las cuotas se verifican con y sin selección de retención, y los resultados
disponibles por capacidad se contrastan con el cálculo por monto.

La configuración central alimenta nuevo crédito, CNCA y la cuota de crédito
en seguros. Tickets, PDF y compartir consumen esa cuota calculada. No se
modifican primas de seguro, tasas publicadas, mínimos ni reglas CNCA.
El total de referencia es cuota × 60; una tabla de amortización que ajuste
el último pago para cerrar saldo tiene una semántica diferente.

## Calibración inicial de productos base

Fuente: `qwdqdssdas.pdf` aportado por el usuario y los dos JSON escaneados el
2026-09-08 a las 20:01:37 y 20:02:12 UTC. Los JSON corroboran los casos de
$205,000 y $40,000; no son observaciones adicionales independientes.

Identificación: OPCIPRES con CAT 42.99% corresponde a OPC base y CONSUBANCO
con CAT 39.03% a CSB base. Todas estas referencias son a 60 meses. Las filas
de CAT 36.42% y 32.94% corresponden a OPC MAX y CSB ESP, respectivamente,
y no se mezclan con los productos base. Consupago/Más Nómina no se usan
para calibrar los dos productos base.

| Monto | Cuota OPC | Total OPC (60 cuotas) | Cuota CSB | Total CSB (60 cuotas) |
| ---: | ---: | ---: | ---: | ---: |
| 254600 | 9248.55 | 554913.00 | 8778.06 | 526683.60 |
| 30000 | 1089.77 | 65386.20 | 1034.34 | 62060.40 |
| 40000 | 1453.03 | 87181.80 | 1379.11 | 82746.60 |
| 205000 | 7446.79 | 446807.40 | 7067.96 | 424077.60 |

## Método y alcance

El motor calcula `monto / 1000 × factor` con BigDecimal y redondea una sola
vez la cuota a dos decimales con HALF_UP. Cada cuota observada `p` a un monto
`a` delimita el factor al intervalo `[(p - 0.005) × 1000 / a,
(p + 0.005) × 1000 / a)`. Se intersectaron los cuatro intervalos por producto
y se eligió el punto medio redondeado a cinco decimales, comprobando que
permanece dentro de la intersección:

| Producto | Intervalo compatible aproximado (superior excluido) | Factor anterior | Factor calibrado |
| --- | --- | ---: | ---: |
| OPC | [36.3257855459544, 36.3258248232522) | 36.32439 | 36.32581 |
| CSB | [34.4778333333333, 34.4778672427337) | 34.47751 | 34.47785 |

Los ocho resultados coinciden al centavo sin excepciones por monto. Los
totales anteriores representan 60 cuotas redondeadas; no incluyen cargos
adicionales no documentados. Las tasas nominales “exactas” de los JSON
varían por monto: son valores inversos compatibles con cuotas redondeadas,
no evidencia de una tasa contractual única. No se sustituyen las tasas
publicadas por esas estimaciones ni se usa el CAT como tasa de amortización.

La calibración no identifica de forma única el algoritmo interno del banco
ni acredita exactitud en montos no observados. No se extrapola a otros plazos,
seguros, reglas CNCA o variantes premium. Para calibrar esos casos hacen
falta referencias específicas. La configuración compartida aplica los nuevos
factores a ambos modos del motor: por monto y por capacidad.

Pruebas: `PortalComparisonRegressionTest.baseProductsMatchUserPaymentsAndTotalsAtSixtyMonths`
comprueba las ocho cuotas y los ocho totales sin tolerancias numéricas.

## Actualización CSB VIP — 2026-10-21

La guía del factor de cálculo de Consubanco VIP fija el factor en
**0.0309541837** por peso (**30.9541837** por $1,000). Se obtiene del CAT con
IVA de 31.5 % tratado como tasa efectiva anual: `i = 1.315^(1/12) - 1`
(2.308208 % mensual con IVA) y la anualidad `i / (1 - (1 + i)^-60)`.
Sustituye a 30.9541845; ambos están dentro de la intersección compatible
con el Excel, así que las 32 cuotas no cambian. La guía añade estas
referencias, cubiertas en `PortalComparisonRegressionTest`:

| Monto | Cuota |
| ---: | ---: |
| 157154.85 | 4864.60 |
| 341400.00 | 10567.76 |
| 150000.00 | 4643.13 |

El primer renglón de la tabla de amortización de $157,154.85 es: interés
$3,127.12, IVA $500.34, abono a capital $1,237.14. Por capacidad, la app
redondea el monto hacia abajo a múltiplos de $100 (véase la regla de
2026-11-02): $6,000 da $193,800, no los $193,834.86 del ejemplo de la guía.

## Actualización CSB ESP y OPC MAX — 2026-10-29

Catorce simulaciones oficiales a 60 meses (siete por producto) mostraron
dos diferencias de un centavo con los factores anteriores: CSB ESP por
$136,000 (31.62828 daba $4,301.45; oficial $4,301.44) y OPC MAX por
$233,333 (33.257255 daba $7,760.02; oficial $7,760.01).

Igual que CSB VIP, el factor se obtiene del CAT con IVA tratado como tasa
efectiva anual: `i = (1 + CAT)^(1/12) - 1` y `1000 × i / (1 - (1 + i)^-60)`.

| Producto | CAT | Factor anterior | Factor nuevo | Intervalo compatible |
| --- | ---: | ---: | ---: | --- |
| CSB ESP | 32.94 % | 31.62828 | **31.628272** | [31.6282683, 31.6282721) |
| OPC MAX | 36.42 % | 33.257255 | **33.2572543** | [33.2572500, 33.2572547) |

Los intervalos incluyen los casos del Excel 2026-09-08, el portal y
$250,000 → $8,314.31 de OPC MAX. Con pasos de $500, los montos por capacidad
no cambiaron: $11,709.34 daba $370,000 (CSB ESP) y $352,000 (OPC MAX).

CSB ESP por $99,999 y $89,995 sirven para calibrar, pero quedan bajo el
mínimo vigente de $100,000 de la app, que no se modifica. El total de
OPC MAX por $190,000 venía como $379,132; corresponde a $379,132.80.

## Actualización CSB VIP y OPC ESP — 2026-11-02

Fuente: `resumen_de_motor_de_c_lculo.md` (plataforma de origen, 60 meses).

**OPC ESP cambia de condiciones.** Pasa de CAT 34.21 % a **CAT 35.31 %**, con
factor **32.737765** y tasa **2.20 %** mensual sin IVA (inferida del CAT:
`(1.3531^(1/12) - 1) / 1.16 = 2.2000 %`). Las siete cuotas de OPC ESP del Excel
2026-09-08 no son compatibles con el factor nuevo y se retiraron de
`ExcelQuoteRegressionTest`, que queda con 25 casos. `getNormalizedRate` reconoce
el CAT nuevo y conserva 34.21 % para créditos vigentes. El mínimo sigue en $75,000.

**CSB VIP** pasa de 30.9541837 a **30.954185**. Ambos reproducen las 3 cuotas del
resumen, los 7 casos del Excel y las 13 simulaciones oficiales, así que los datos
no permiten distinguirlos; difieren un centavo en 128 de los 1,701 múltiplos de
$500 entre $150,000 y $1,000,000, por ejemplo $162,500 ($5,030.05 contra $5,030.06).

El resumen describe el factor como truncado a 9 decimales. El valor exacto
derivado del CAT truncado sería 0.030954183, que queda fuera del intervalo
compatible; se usa 0.030954185 tal como lo publica la plataforma. Las reglas de
`Number.EPSILON` no aplican: el motor usa BigDecimal con HALF_UP.

## Incremento de monto — 2026-11-02

**Por descuento**, el monto se redondea hacia abajo a múltiplos de $100 (el
`amountStep` del convenio) sin superar el descuento capturado. **Por monto** no
hay incremento: se acepta cualquier cantidad desde el mínimo, con decimales. IMSS Bienestar, desactivado, conserva su incremento de $500.

## Actualización CSB ESP y OPC MAX — 2026-11-02

Ocho simulaciones oficiales a 60 meses (cuatro por producto: $100,000, $199,999,
$333,333 y $456,789) confirman nuevas condiciones. Ningún monto coincidía con los
factores anteriores.

| Producto | CAT anterior | CAT nuevo | Tasa inferida | Factor nuevo | Intervalo compatible |
| --- | ---: | ---: | ---: | ---: | --- |
| CSB ESP | 32.94 % | **33.48 %** | 2.10 % | **31.8810807** | [31.8810769, 31.8810844) |
| OPC MAX | 36.42 % | **37.16 %** | 2.30 % | **33.6034742** | [33.6034690, 33.6034909) |

Los factores salen del CAT (`i = (1 + CAT)^(1/12) - 1`). Las tasas son aproximadas:
del CAT resultan 2.0997 % y 2.3001 %, y una anualidad con 2.10 % o 2.30 % exactos
queda fuera de los intervalos. Los casos del Excel y del portal con los CAT
anteriores se retiraron de las pruebas; `ExcelQuoteRegressionTest` queda con 15
casos. `getNormalizedRate` conserva los CAT anteriores para créditos vigentes.

Con cuatro montos el intervalo es más ancho que en calibraciones anteriores, en
especial en OPC MAX: faltan montos altos y casos por descuento para cerrarlo.

## Mínimos y plazos de MAX, ESP y VIP — 2026-11-02

Confirmado por el usuario: plazo de 60 meses y mínimos de $50,000 (OPC MAX),
$75,000 (OPC ESP), $100,000 (CSB ESP) y $150,000 (CSB VIP), sin monto máximo.
El tope de $1,000,000 del convenio sigue aplicando solo a OPC y CSB base.
