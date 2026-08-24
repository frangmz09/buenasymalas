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
- Fin de partida con CameraX: resultado, apodo del perdedor y foto guardada en el
  almacenamiento interno. La partida se persiste al llegar al objetivo.
- Tema propio para los diálogos, que venían con el gris de fábrica.
- Historial cabeza a cabeza: récord, racha actual, barra de proporción y últimas
  partidas con miniatura de la foto.
- El museo de las chicanas: grid de fotos con filtro por jugador y por paliza,
  contador de derrotas documentadas.
- Perfil de jugador: stats, rivales con etiqueta ("invicto" / "lo domina" /
  "lo sufre"), elección de mitad de cancha con preview del marcador, peor derrota
  y edición de nombre y alias.
- Navegación completa entre las 6 pantallas: inicio → marcador → foto → museo,
  y desde el perfil de un jugador a su cabeza a cabeza con cada rival.
