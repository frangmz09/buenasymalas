# Handoff: Buenas y Malas — contador de truco

## Overview
App **offline** para llevar el marcador de una partida de truco con los cuadraditos clásicos
(cuadrado de 4 palitos + diagonal = 5 puntos), con perfiles de jugador, historial cabeza a
cabeza entre integrantes, y una foto del perdedor al final de la partida que alimenta una
galería de chicanas. Sin cuentas, sin backend, sin red: todo vive en el teléfono.

**Nombre del producto:** Buenas y Malas.

## About the Design Files
Los archivos de este bundle son **referencias de diseño hechas en HTML** — prototipos que
muestran el aspecto y el comportamiento buscados, no código de producción para copiar.
La tarea es **recrear estos diseños en el entorno de la app destino** (Android nativo, ver
abajo) usando sus patrones y librerías. El HTML no se embebe ni se porta: se lee como
especificación visual.

**Objetivo declarado por el usuario:** Android nativo en **Java** (Android Studio).
La UI dibuja bien con Views + XML; el marcador conviene resolverlo como `CustomView` con
`Canvas` (ver "Marcador"). Si el desarrollador prefiere Kotlin/Compose, el diseño se traduce
sin cambios conceptuales, pero el pedido original es Java.

## Fidelity
**Alta fidelidad (hifi)** para color, tipografía y espaciado; los prototipos usan datos de
ejemplo. Recrear la UI con estos valores exactos. Los mockups están dibujados a 340×720 px
(pantalla dentro del bezel), o sea densidad ~1x de un teléfono de 360dp de ancho: los px del
prototipo se leen directamente como **dp**.

---

## Design Tokens

### Colores
| Token | Hex | Uso |
|---|---|---|
| madera | `#3A2317` | fondo principal, mitad de cancha por defecto |
| madera oscura | `#2B1A11` | fondos de pantalla, tarjetas oscuras |
| madera negra | `#1A0F09` | bezel, barra de gestos, fondo de fin de partida |
| barra inferior | `#22140D` | barra de acciones del marcador |
| paño | `#33301C` | mitad de cancha del equipo B |
| tiza | `#F4EFE4` | texto sobre oscuro, fondos claros |
| tiza atenuada | `rgba(244,239,228,.45)` | texto secundario sobre oscuro |
| bordó | `#8A2B20` | acción primaria, acentos, derrota |
| bordó sombra | `#5D1C14` | sombra sólida 3–4px de los botones primarios |
| cobre | `#C98F57` | etiquetas, línea divisoria, chips activos |
| cobre claro | `#E8C69B` | texto sobre chip cobre translúcido |
| verde | `#3D6B4A` | victoria, valores positivos |
| papel | `#E8E2D6` / `#FFF` | fondos claros (historial, perfil) |

Paleta de "mitad de cancha" elegible por jugador:
`#3A2317` madera · `#2F4636` paño · `#5D1C14` bordó · `#23304A` azul noche ·
`#1A1A18` pizarra · `#6B4A1F` roble.

### Textura de madera
Fondo de las superficies de mesa:
`repeating-linear-gradient(93deg, rgba(0,0,0,.16) 0 2px, transparent 2px 9px)` sobre el color
base. En Android: un `Drawable` de rayas diagonales tenues o un tile PNG de 1x9dp rotado.

### Tipografía
- **Alfa Slab One** (Google Fonts) — títulos, nombres de jugador, números del marcador.
  Tamaños usados: 40 / 34 / 30 / 28 / 27 / 22 / 21 / 20 / 19 (botón primario) / 66 y 62 (score grande) / 46 / 26 / 24.
- **Archivo** 400/500/600/700 — toda la UI. Tamaños: 15 / 14 / 13 / 12 / 11 / 10.
- **Archivo Narrow** 600/700 — etiquetas en mayúscula con `letter-spacing` 1.4–3px, tamaños 9–11.

### Espaciado y forma
- Padding de pantalla: 18–22dp horizontal.
- Gaps verticales: 9 / 12 / 14–16dp.
- Radios: 30 (bezel), 24 (pantalla), 20 (bottom sheet superior), 13 (botón primario),
  11–12 (tarjetas y botones), 10 (filas), 9 (chips cuadrados), 20+ (chips píldora), 50% (avatares).
- Sombra de tarjeta flotante: `0 10px 30px rgba(0,0,0,.28)`.
- Botón primario: sombra sólida `0 4px 0 #5D1C14` (efecto de tecla).

---

## Screens / Views

### 1. Inicio — "armar la mesa" (`1a`)
**Propósito:** elegir formato, reglas, jugadores y quién es mano; arrancar la partida.

Layout, de arriba a abajo:
1. Status bar del sistema.
2. Cabecera sobre madera: título `TRUCO` (Alfa Slab One 40, tiza) + kicker
   `CONTADOR DE LA MESA` (Archivo Narrow 11, cobre, letter-spacing 3).
3. Panel translúcido (`rgba(244,239,228,.05)`, borde superior `rgba(244,239,228,.12)`),
   padding 16/18, que ocupa el resto:
   - **FORMATO** — fila de 3 botones iguales (1 v 1 / 2 v 2 / 3 v 3), radio 9, alto ~40dp.
     Activo: fondo tiza, texto madera oscura, 700. Inactivo: borde `rgba(244,239,228,.25)`.
   - Fila de chips píldora, envolvente: Con flor / Sin flor / A 15 / A 30. Activo: fondo
     `rgba(201,143,87,.22)`, borde cobre, texto `#E8C69B`. Inactivo: borde tenue.
     Son dos grupos independientes (flor sí/no, puntaje 15/30).
   - **EN LA MESA** — lista de jugadores. Cada fila: fondo `rgba(0,0,0,.22)`, radio 11,
     avatar circular de 38dp con la inicial (Alfa Slab One 17) en el color del jugador,
     nombre (Archivo 700 15) + línea de stats (Archivo 500 11, tiza .45), y a la derecha
     un botón **MANO** (radio 7, padding 5/9): activo = fondo cobre con texto madera;
     inactivo = borde `rgba(244,239,228,.25)`, texto tiza .5. Tocarlo pasa la mano a ese
     jugador (exclusivo: solo uno puede ser mano).
   - Hint bajo la lista: "Tocá MANO para elegir quién reparte primero."
   - Fila punteada "+ Sumar jugador" (borde `1px dashed rgba(244,239,228,.22)`).
4. Pie: línea de chicana contextual generada del historial ("Nacho perdió las últimas 3. Va la
   revancha.") + botón primario **Que empiece** (bordó, Alfa Slab One 19, sombra sólida).

### 2. Marcador en partida (`1b`) — pantalla central
**Propósito:** contar puntos durante la partida.

**División vertical: dos columnas iguales, una por equipo**, separadas por una línea punteada
vertical de 3dp (`repeating-linear-gradient(180deg, #C98F57 0 14px, transparent 14px 26px)`).
Columna izquierda con fondo madera `#3A2317`, derecha con paño `#33301C` (o el color de
"mitad de cancha" del perfil de cada jugador).

Cada columna, centrada, de arriba a abajo (padding 16/12/14):
- Nombre del equipo/jugador — Alfa Slab One 21, tiza.
- Zona actual — `MALAS` o `BUENAS`, Archivo Narrow 10, cobre, letter-spacing 2.
- Puntaje numérico — Alfa Slab One 66, tiza.
- **Marcador de cuadraditos** (ver abajo): dos bloques apilados, `MALAS` y `BUENAS`, cada uno
  con su etiqueta arriba y 3 cuadrados de 40×40dp en fila con gap 5.
- Al pie (empujado con `margin-top:auto`): botones **−** y **+**, 52×48dp, radio 12.
  `+`: fondo `rgba(244,239,228,.14)`, borde `rgba(244,239,228,.3)`, texto tiza.
  `−`: solo borde `rgba(244,239,228,.2)`, texto tiza .6.

**Gesto principal:** un tap en cualquier parte de la mitad de un equipo suma **1** punto a ese
equipo. Los botones − / + del pie hacen lo mismo de forma explícita (el tap en el botón no
debe propagarse al panel: sumaría dos veces).

Barra inferior (`#22140D`, padding 12/16): tres botones iguales —
**Deshacer** (borde tenue), **Nueva** (borde tenue), **Cantos** (fondo `rgba(201,143,87,.2)`,
borde cobre, texto `#E8C69B`). Debajo, una línea de estado de 10px, tiza .35, centrada:
"<nombre> arriba por N · un tap suma 1", o "Partida terminada — sacá la foto" al llegar al
objetivo.

#### Marcador (los cuadraditos) — regla de dibujo
- Un cuadrado = **5 puntos**. Se dibuja con 5 trazos, en este orden:
  1. lado superior, 2. lado derecho, 3. lado inferior, 4. lado izquierdo, 5. **diagonal**
  de la esquina superior izquierda a la inferior derecha (el 5º punto).
- Geometría del prototipo: viewBox 44×44, cuadrado de (8,8) a (36,36), trazo 3.4,
  extremos redondeados, color tiza `#F4EFE4`. Trazo no ganado: la misma línea al **13% de
  opacidad** (se ve el fantasma del cuadrado, como marcas tenues de tiza).
- Fila **MALAS** = puntos 1–15 → 3 cuadrados. Fila **BUENAS** = puntos 16–30 → 3 cuadrados.
  En partida a 15: 3 cuadrados en malas y 3 en buenas de 5 puntos cada mitad (7 y 8), o
  simplemente una sola fila de 3 — definir con el usuario si se implementa el modo a 15.
- Puntaje máximo 30, mínimo 0 (clamp).
- En Java: `ScoreBoardView extends View`, `onDraw` recorre 3 grupos × 5 segmentos y dibuja con
  `Paint` (`STROKE`, `strokeCap = ROUND`), aplicando `alpha` 255 o 33 según corresponda.
  Animar la aparición del último trazo (200ms) es un plus.

### 3. Hoja de cantos (bottom sheet sobre el marcador)
Se abre con el botón **Cantos**. Scrim `rgba(10,6,4,.6)` sobre toda la pantalla; tocar el
scrim o "Cerrar" la baja. Hoja de fondo papel `#F4EFE4`, radio superior 20, padding 14/16,
con un handle de 44×4dp centrado.

Cabecera: "Cantos" (Alfa Slab One 22, madera oscura) + hint a la derecha
"tocá a quién se le suma" (Archivo 500 11).

Cada fila (fondo blanco, borde `rgba(43,26,17,.1)`, radio 10, padding 7/9):
nombre del canto (Archivo 700 13) + aclaración (Archivo 500 10, "no querido: N"),
puntos en Alfa Slab One 19 bordó, y **dos botones** con el nombre de cada jugador
(padding 8/11, radio 9, fondo del color de su mitad de cancha, texto tiza) que suman esos
puntos a ese lado y cierran/mantienen la hoja abierta según preferencia.

Cantos y valores:
| Canto | Puntos | Si no lo quieren |
|---|---|---|
| Envido | 2 | 1 |
| Real envido | 3 | 1 |
| Falta envido | lo que le falta al que va arriba (`30 − max(a,b)`, mínimo 1) | 1 |
| Truco | 2 | 1 |
| Retruco | 3 | 2 |
| Vale cuatro | 4 | 3 |
| Flor | 3 | de entrada |
| Contraflor | 6 | 4 |

La fila de Flor/Contraflor solo aparece si la partida se configuró **con flor**.

### 4. Fin de partida + foto (`1c`)
Fondo `#1A0F09`. Arriba, centrado: kicker `SE ACABÓ` (cobre), resultado
"Rocho 30 — 22 Nacho" (Alfa Slab One 34), y un chip bordó con el **apodo fijo del perdedor**
en mayúsculas ("NACHO ES EL PERRO", Archivo 700 12).

Centro: **visor de cámara en vivo** a pantalla casi completa (margen 18, radio 14). Overlay
superior con "CÁMARA FRONTAL" / "FLASH". Controles abajo dentro del visor: "Galería" ·
obturador circular de 62dp (borde tiza 4dp, relleno bordó) · "Girar".

Pie: explicación ("La foto se sella con el marcador, la fecha y el apodo. Queda en la galería
de chicanas.") y dos botones: **Saltear** (secundario) y **Sellar y guardar** (primario bordó,
flex 1.4 contra 1).

Comportamiento: CameraX, cámara frontal por defecto. Al guardar, se compone la foto con el
marcador final, la fecha y el apodo quemados en la imagen (o guardados como metadatos y
dibujados en la vista de galería — preferible para poder cambiarlos después).

### 5. Historial cabeza a cabeza (`1d`)
Cabecera sobre madera: kicker `CABEZA A CABEZA`, título "Rocho vs Nacho" (Alfa Slab One 27),
fila con los dos totales (Alfa Slab One 46; el del que va perdiendo a tiza .55) y a la derecha
"racha actual / <nombre> ×N". Debajo, una barra de proporción de 8dp de alto, radio 4,
partida en bordó y `rgba(244,239,228,.25)`.

Cuerpo con fondo papel `#F4EFE4`: lista **ÚLTIMAS PARTIDAS**. Cada fila es una tarjeta blanca
(radio 10, borde `rgba(43,26,17,.1)`, padding 11/12) con: barra vertical de 6×34dp del color
del ganador (bordó/verde), resultado (Archivo 700 14), meta (fecha, formato, "paliza",
Archivo 500 11 al 50%), y miniatura de 38dp de la foto si la hay.

### 6. Galería de chicanas — "El museo" (`1e`)
Título "El museo" (Alfa Slab One 30) + contador de derrotas. Fila de filtros en chips
(Todas / por jugador / Palizas). Grid de 2 columnas, gap 10: cada ítem es una tarjeta oscura
radio 11 con la foto arriba (~104dp) y un pie `rgba(244,239,228,.06)` con título de la chicana
(Archivo 700 12) y meta (resultado + fecha, Archivo 500 10). Al pie de la pantalla:
"Todo queda en el teléfono. Sin cuenta, sin internet."

### 7. Perfil de jugador (`1f`)
Cabecera bordó con avatar de 60dp (círculo tiza, inicial bordó), nombre (Alfa Slab One 28) y
`ALIAS: EL PERRO` (Archivo 600 11, letter-spacing 1.5).

Fila de 3 stats sobre blanco, separadas por bordes de 1px: Jugadas (madera), Ganadas (verde),
Perdidas (bordó); número en Alfa Slab One 24 y etiqueta en Archivo Narrow 10.

Cuerpo papel:
- **RIVALES** — filas blancas con nombre, récord ("7 — 13") y una etiqueta coloreada
  ("lo sufre" bordó / "lo domina" y "invicto" verde).
- **SU MITAD DE CANCHA** — 6 swatches de 38dp radio 10 con los colores de la paleta de arriba;
  el elegido lleva doble anillo (`0 0 0 2px #F4EFE4, 0 0 0 4px #2B1A11`). Debajo, una vista
  previa de 56dp de alto del marcador partido en dos con ese color aplicado a su mitad, y la
  aclaración "Su lado del marcador siempre se pinta de este color, juegue donde juegue."
- Tarjeta oscura **PEOR DERROTA** con el resultado y la fecha.
- Botón secundario "Editar nombre y alias".

---

## Interactions & Behavior
- **Sumar punto:** tap en la mitad del equipo (+1) o botón `+`. Botón `−` resta 1.
  Clamp 0–objetivo. Los taps en los botones no deben propagar al panel.
- **Deshacer:** pila de los últimos ~20 estados (a, b); restaura el anterior.
- **Nueva:** vuelve a 0–0 y limpia la pila. Pedir confirmación si la partida está avanzada.
- **Fin de partida:** cuando un equipo llega al objetivo, cambia la línea de estado y se
  navega (o se ofrece navegar) a la pantalla de foto.
- **Mano:** exclusiva, se toca para pasarla. Se guarda con la partida.
- **Cantos:** la hoja suma los puntos del canto al equipo elegido; la misma acción entra en la
  pila de deshacer como un solo paso.
- **Apodo del perdedor:** apodo fijo por jugador, configurable en el perfil; se muestra en
  inicio, fin de partida, galería y perfil.
- Mantener la pantalla encendida durante la partida (`FLAG_KEEP_SCREEN_ON`).
- Todo funciona en avión: sin permisos de red. Único permiso: cámara (y almacenamiento según API).

## State Management
Estado de partida en curso (sobrevive rotación y muerte del proceso — `ViewModel` +
`SavedStateHandle`):
```
scoreA, scoreB      int, 0..target
target              15 | 30
withFlor            bool
format              ONE_V_ONE | TWO_V_TWO | THREE_V_THREE
manoPlayerId        long
undoStack           List<int[]>   // pares (a, b), máx 20
sheetOpen           bool
```

Persistencia (Room):
```
Player   id, name, alias, colorHex, avatarInitial, createdAt
Team     id, matchId, name          // para 2v2 / 3v3
TeamMember  teamId, playerId
Match    id, playedAt, target, withFlor, format, scoreA, scoreB,
         winnerTeamId, manoPlayerId
Photo    id, matchId, filePath, caption, loserPlayerId
```
Derivados por consulta: récord cabeza a cabeza, racha actual, peor derrota, total de derrotas
documentadas.

## Assets
Ninguno propietario. Las fotos las genera el usuario. Fuentes: **Alfa Slab One** y
**Archivo / Archivo Narrow** (Google Fonts, OFL) — empaquetarlas en `res/font/`.
Los placeholders rayados de los prototipos representan fotos del usuario: no reproducirlos.
No hay iconografía definida; usar Material Symbols o pedir set de iconos.

## Files
- `Contador de Truco.dc.html` — las 6 pantallas, en un lienzo. Los ids `1a`…`1f` corresponden
  a las secciones de este documento. El marcador (`1b`) y la hoja de cantos son interactivos:
  abrir el archivo en un navegador y probarlos.
- `support.js` — runtime del prototipo, no forma parte del diseño.

## Preguntas abiertas para el desarrollador
1. Modo a 15: ¿cómo se reparten los cuadrados entre malas y buenas?
2. 2v2 / 3v3: ¿el historial es por equipo armado o por jugador?
3. ¿La hoja de cantos se cierra al sumar o queda abierta para encadenar?
4. Exportar/compartir una foto sellada: ¿hace falta, o queda todo dentro de la app?
