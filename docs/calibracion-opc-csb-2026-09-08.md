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
