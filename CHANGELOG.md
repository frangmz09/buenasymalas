# Changelog

Todos los cambios notables de este proyecto se documentan acá.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado sigue [SemVer](https://semver.org/lang/es/).

## [Unreleased]

### Added
- Proyecto Gradle con AGP 8.7.3, Java 17, minSdk 26 y viewBinding.
- Design system: tokens de color, dimensiones, estilos de texto por rol y las tres
  familias tipográficas (Alfa Slab One, Archivo, Archivo Narrow) empaquetadas.
- `WoodDrawable`, la textura de mesa que acepta el color base por parámetro.
- `DashedLineView`, la línea punteada de tiza que parte la cancha.
- `MainActivity` con Navigation Component y la pantalla de inicio en esqueleto.
- Persistencia con Room: jugadores, equipos, partidas y fotos, detrás de un
  `TrucoRepository` único.
- Historial por jugador y por equipo armado: cada equipo guarda un `rosterKey` que
  le da identidad estable a la dupla entre partidas.
- `ScoreBoardView`, el marcador de cuadraditos dibujado en `Canvas`, con animación
  del trazo recién ganado.
- Pantalla de marcador: dos mitades de cancha, un tap por punto, deshacer de veinte
  pasos y la pantalla que no se apaga durante la partida.
- Hoja de cantos con envido, truco y flor; el falta envido se calcula sobre el
  marcador del momento.
- Pantalla de inicio: formato, reglas, alta y edición de jugadores con su color de
  mitad de cancha, mano exclusiva y la chicana del pie sacada del historial.
