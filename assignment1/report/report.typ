#import "@preview/lilaq:0.4.0" as lq

#align(center, text(18pt)[*Assignment 1 di Programmazione Concorrente e Distribuita 2025/2026*])
#align(center, text(12pt)[Mattia Ronchi, matr. 0001236997 \ Samorì Andrea 0001235969 \ Andrea Monaco 000])

= Analisi del problema

L'assignment ha l'obiettivo di realizzare una versione concorrente del gioco `Poool` fornito in 2 varianti:
1. Variante multi-threading, utilizzando i default/platform threads
2. Variante task-based, utilizzando il Java Executor Framework

= Aspetti rilevanti per la concorrenza

La logica principale del programma consiste nell'aggiornare lo stato del sistema, composto da un insieme di palle (giocatore e CPU) e palline, a seguito delle collisioni tra esse. Dato il costo quadratico ($O(n^2)$) del calcolo delle collisioni tra $n$ palline e dato che $n$ può aumentare significativamente (vedi `SmallBoard` vs `MassiveBoard`), la parte computazionalmente più dispendiosa consiste nel calcolare le collisioni tra queste ed è quella su cui ci siamo concentrati. La realizzazione di una versione concorrente di questo calcolo deve prestare attenzione a possibili accessi concorrenti a una singola pallina da parte di entità computazionali distinte.

= Design della soluzione

== Definizione delle task

La task principale è la risoluzione delle collisioni tra tutte le coppie di palline. La seguente è la porzione di codice sequenziale che abbiamo intenzione di rendere concorrente:

```java
for (int i = 0; i < balls.size() - 1; i++) {
  for (int j = i + 1; j < balls.size(); j++) {
      Ball.resolveCollision(balls.get(i), balls.get(j));
  }
}
```

La risoluzione della collisione opera su due proprietà di ogni palla: posizione e velocità.

== Calcolo delle collisioni

Il calcolo delle collisioni è diviso in 2 parti:
1. risultati parziali dell'interazione tra le singole palline
2. aggregazione di tutti i risultati parziali di ogni pallina

Durante la prima fase, posizione e velocità della palla rimangono costanti e vengono modificate una e una sola volta nella seconda fase. I risultati parziali sono salvati in strutture dati apposite durante la prima fase e solo nella seconda verranno aggregati. È presente una dipendenza temporale tra le fasi e richiede una sincronizzazione.

// I risultati parziali sono gli scostamenti rispetto ai valori di posizione e velocità della palla.
// ```java
// a.collisionMonitor.addPosition(a_deltap);
// ...
// a.collisionMonitor.addVelocity(a_deltav);
// ```

// Nell'esempio è riportata l'aggregazione relativa alla posizione.
// ```java
// var positionDisplacements = ball.collisionMonitor.getPositionDisplacements();
// double avgDx = positionDisplacements.stream().mapToDouble(P2d::x).average().orElse(0);
// double avgDy = positionDisplacements.stream().mapToDouble(P2d::y).average().orElse(0);
// ball.pos = new P2d(ball.pos.x() + avgDx, ball.pos.y() + avgDy);
// ```

== Architettura

L'architettura concorrente impiega delle entità computazionali attive a cui viene delegato il calcolo delle collisioni. Queste entità utilizzano dei componenti passivi per la coordinazione e l'aggregazione dei risultati.

Un'entità comune a entrambe le soluzioni è il `CollisionMonitor`: un monitor utilizzato per salvare i risultati delle singole collisioni e, solo al termine di tutti i calcoli, aggregarli applicando l'effetto finale alla palla. La prima parte del calcolo è effettuata in maniera concorrente, la seconda in maniera seriale una volta che la prima parte è conclusa.

Le varianti differiscono nella sincronizzazione tra le due fasi.

=== Variante multi-threading

Ciascun thread è realizzato attraverso `CollisionResolverWorker`. Internamente esso mantiene due semafori ad eventi: uno (`startWorkSem`) attraverso cui viene notificato dell'inizio della prima fase del calcolo delle collisioni; l'altro (`endWorkSem`) attraverso cui notifica di aver terminato il lavoro assegnatogli. Questa entità è creata inizialmente un'unica volta e rimane sempre attiva, in attesa della disponibilità del lavoro.

```java
while (true) {
  try {
      startWorkSem.acquire();
  } catch (InterruptedException e) {
      throw new RuntimeException(e);
  }

  ... (resolving collision)

  endWorkSem.release();
}
```

All'interno di `Board` è presente il `CollisionResolverManager`: l'entità computazionale responsabile della creazione, dell'esecuzione e della gestione dei thread (`CollisionResolverWorker`). Internamente esso mantiene un semaforo ad eventi (`endWorkSem`) per gestire la sincronizzazione tra le due fasi di calcolo della collisione (aspettare la terminazione dei lavori di tutti i thread).

```java
public void startWork() {
    for (var w : workers) w.startWork(); // startWorkSem.release() da parte del worker
}

public void waitForWorkEnd() throws InterruptedException {
    endWorkSem.acquire(numWorkers);
}
```

=== Variante task-based

All'interno di `Board` la gestione delle task è affidata a un `ExecutorService` e la task è modellata attraverso `CollisionResolvingTask`. Quest'ultima mantiene una barriera ciclica (`CyclicBarrier`) condivisa con la `Board` per realizzare la sincronizzazione tra le due fasi di calcolo della collisione.

Seguendo l'approccio task-oriented, le task vengono create e assegnate ad ogni aggiornamento dello stato (`updateState`).
```java
for (int i = 0; i < nCores; i++) {
    executor.execute(new CollisionResolvingTask(barrier, this.balls, i, nCores));
}
```

= Performance

Abbiamo osservato che la porzione di programma non parallelizzabile è piuttosto elevata e abbiamo deciso di misurare le performance in 2 situazioni:
1. Considera solamente le modifiche da noi effettuate, rimuovendo il più possibile l'overhead esterno. Le misure sono effettuate con il numero di thread/task crescente. L'unità di misura sono i millisecondi necessari per effettuare un aggiornamento dello stato. Il programma misura il tempo di aggiornamento medio per un certo numero di iterazioni.
2. Comprende il programma nella sua interezza. Le misure riguardano il solo caso massimo.

Le performance sono state misurate con una macchina con processore X con N core bla bla bla.

== Speedup
Tabella risultati sistema seriale:
#table(
  columns: 2,
  table.header([Interazione], [Tempo medio ms]),
  [100], [152.72],
  [200], [158.33],
  [300], [159.20],
  [400], [159.00],
  [500], [159.51],
  [600], [159.85],
  [700], [159.96],
  [800], [159.35],
  [900], [160.51],
  [1000], [159.74],
  [1100], [160.29],
  [1200], [160.31],
  [1300], [159.88],
  [1400], [154.29],
  [1500], [149.04],
  [1600], [149.65],
  [1700], [149.44],
  [1800], [149.18],
  [1900], [149.96],
  [2000], [149.22],
)

Tabella risultati sistema concorrente (threads):
#table(
  columns: 3,
  table.header([Cores], [Tempo medio ms], [medie]),
  [1],
  [154.22],
  [Avg at pass 100 : 160.83
    Avg at pass 200 : 158.82
    Avg at pass 300 : 160.33
    Avg at pass 400 : 152.6
    Avg at pass 500 : 152.44
    Avg at pass 600 : 152.48
    Avg at pass 700 : 152.63
    Avg at pass 800 : 152.59
    Avg at pass 900 : 155.71
    Avg at pass 1000 : 152.61
    Avg at pass 1100 : 152.5
    Avg at pass 1200 : 152.78
    Avg at pass 1300 : 152.57
    Avg at pass 1400 : 153.53
    Avg at pass 1500 : 152.81
    Avg at pass 1600 : 152.97
    Avg at pass 1700 : 153.47
    Avg at pass 1800 : 153.33
    Avg at pass 1900 : 153.24
    Avg at pass 2000 : 156.1],

  [2],
  [83.16],
  [Avg at pass 100 : 83.43
    Avg at pass 200 : 83.33
    Avg at pass 300 : 84.49
    Avg at pass 400 : 82.85
    Avg at pass 500 : 82.75
    Avg at pass 600 : 82.89
    Avg at pass 700 : 82.98
    Avg at pass 800 : 83.26
    Avg at pass 900 : 83.09
    Avg at pass 1000 : 83.0
    Avg at pass 1100 : 83.11
    Avg at pass 1200 : 82.96
    Avg at pass 1300 : 83.05
    Avg at pass 1400 : 83.11
    Avg at pass 1500 : 83.09
    Avg at pass 1600 : 83.33
    Avg at pass 1700 : 83.32
    Avg at pass 1800 : 82.91
    Avg at pass 1900 : 83.21
    Avg at pass 2000 : 83.1],

  [3],
  [57.18],
  [Avg at pass 100 : 57.5
    Avg at pass 200 : 57.66
    Avg at pass 300 : 57.46
    Avg at pass 400 : 56.98
    Avg at pass 500 : 56.95
    Avg at pass 600 : 56.99
    Avg at pass 700 : 57.02
    Avg at pass 800 : 57.09
    Avg at pass 900 : 57.08
    Avg at pass 1000 : 57.09
    Avg at pass 1100 : 57.33
    Avg at pass 1200 : 57.08
    Avg at pass 1300 : 57.17
    Avg at pass 1400 : 57.21
    Avg at pass 1500 : 57.19
    Avg at pass 1600 : 57.11
    Avg at pass 1700 : 57.19
    Avg at pass 1800 : 57.24
    Avg at pass 1900 : 57.18
    Avg at pass 2000 : 57.17],

  [4],
  [44.29],
  [Avg at pass 100 : 44.74
    Avg at pass 200 : 44.25
    Avg at pass 300 : 44.84
    Avg at pass 400 : 43.93
    Avg at pass 500 : 43.92
    Avg at pass 600 : 43.89
    Avg at pass 700 : 43.98
    Avg at pass 800 : 43.95
    Avg at pass 900 : 43.97
    Avg at pass 1000 : 44.19
    Avg at pass 1100 : 43.97
    Avg at pass 1200 : 44.25
    Avg at pass 1300 : 44.16
    Avg at pass 1400 : 47.22
    Avg at pass 1500 : 43.99
    Avg at pass 1600 : 44.03
    Avg at pass 1700 : 44.07
    Avg at pass 1800 : 44.03
    Avg at pass 1900 : 44.14
    Avg at pass 2000 : 44.35],

  [5],
  [36.23],
  [Avg at pass 100 : 39.73
    Avg at pass 200 : 38.25
    Avg at pass 300 : 36.51
    Avg at pass 400 : 35.75
    Avg at pass 500 : 35.8
    Avg at pass 600 : 35.77
    Avg at pass 700 : 35.7
    Avg at pass 800 : 35.89
    Avg at pass 900 : 35.79
    Avg at pass 1000 : 35.91
    Avg at pass 1100 : 35.91
    Avg at pass 1200 : 36.03
    Avg at pass 1300 : 35.93
    Avg at pass 1400 : 36.06
    Avg at pass 1500 : 35.74
    Avg at pass 1600 : 36.4
    Avg at pass 1700 : 35.83
    Avg at pass 1800 : 35.82
    Avg at pass 1900 : 35.82
    Avg at pass 2000 : 35.96],

  [6],
  [33.23],
  [Avg at pass 100 : 41.78
    Avg at pass 200 : 32.78
    Avg at pass 300 : 34.06
    Avg at pass 400 : 32.64
    Avg at pass 500 : 32.13
    Avg at pass 600 : 31.39
    Avg at pass 700 : 31.47
    Avg at pass 800 : 31.22
    Avg at pass 900 : 32.13
    Avg at pass 1000 : 31.54
    Avg at pass 1100 : 32.85
    Avg at pass 1200 : 33.11
    Avg at pass 1300 : 33.16
    Avg at pass 1400 : 32.85
    Avg at pass 1500 : 33.64
    Avg at pass 1600 : 34.94
    Avg at pass 1700 : 33.2
    Avg at pass 1800 : 33.58
    Avg at pass 1900 : 33.07
    Avg at pass 2000 : 32.96],

  [7],
  [43.46],
  [Avg at pass 100 : 46.94
    Avg at pass 200 : 44.6
    Avg at pass 300 : 43.62
    Avg at pass 400 : 44.05
    Avg at pass 500 : 43.56
    Avg at pass 600 : 42.76
    Avg at pass 700 : 44.35
    Avg at pass 800 : 42.13
    Avg at pass 900 : 42.79
    Avg at pass 1000 : 42.96
    Avg at pass 1100 : 44.94
    Avg at pass 1200 : 43.75
    Avg at pass 1300 : 44.35
    Avg at pass 1400 : 42.6
    Avg at pass 1500 : 41.51
    Avg at pass 1600 : 43.82
    Avg at pass 1700 : 43.22
    Avg at pass 1800 : 42.4
    Avg at pass 1900 : 42.63
    Avg at pass 2000 : 42.18],
)

Tabella risultati sistema concorrente (executor):
#table(
  columns: 3,
  table.header([Cores], [Tempo medio ms], [medie]),
  [1],
  [132.59],
  [Avg at pass 100 : 130.0
    Avg at pass 200 : 131.53
    Avg at pass 300 : 136.95
    Avg at pass 400 : 136.44
    Avg at pass 500 : 135.35
    Avg at pass 600 : 134.35
    Avg at pass 700 : 132.33
    Avg at pass 800 : 132.0
    Avg at pass 900 : 131.83
    Avg at pass 1000 : 131.99
    Avg at pass 1100 : 131.78
    Avg at pass 1200 : 131.93
    Avg at pass 1300 : 131.75
    Avg at pass 1400 : 131.61
    Avg at pass 1500 : 131.94
    Avg at pass 1600 : 131.92
    Avg at pass 1700 : 131.98
    Avg at pass 1800 : 132.04
    Avg at pass 1900 : 132.06
    Avg at pass 2000 : 132.08],

  [2],
  [69.39],
  [Avg at pass 100 : 69.02
    Avg at pass 200 : 70.49
    Avg at pass 300 : 70.21
    Avg at pass 400 : 69.38
    Avg at pass 500 : 69.25
    Avg at pass 600 : 69.08
    Avg at pass 700 : 69.03
    Avg at pass 800 : 69.03
    Avg at pass 900 : 69.08
    Avg at pass 1000 : 69.16
    Avg at pass 1100 : 69.08
    Avg at pass 1200 : 69.29
    Avg at pass 1300 : 69.4
    Avg at pass 1400 : 69.26
    Avg at pass 1500 : 69.16
    Avg at pass 1600 : 69.58
    Avg at pass 1700 : 69.39
    Avg at pass 1800 : 69.28
    Avg at pass 1900 : 69.51
    Avg at pass 2000 : 70.14],

  [3],
  [45.71],
  [Avg at pass 100 : 49.64
    Avg at pass 200 : 46.0
    Avg at pass 300 : 45.48
    Avg at pass 400 : 45.67
    Avg at pass 500 : 45.51
    Avg at pass 600 : 45.74
    Avg at pass 700 : 45.47
    Avg at pass 800 : 45.31
    Avg at pass 900 : 45.37
    Avg at pass 1000 : 45.48
    Avg at pass 1100 : 45.36
    Avg at pass 1200 : 45.43
    Avg at pass 1300 : 45.37
    Avg at pass 1400 : 45.82
    Avg at pass 1500 : 45.33
    Avg at pass 1600 : 45.3
    Avg at pass 1700 : 45.34
    Avg at pass 1800 : 45.29
    Avg at pass 1900 : 45.39
    Avg at pass 2000 : 45.81],

  [4],
  [35.89],
  [Avg at pass 100 : 41.4
    Avg at pass 200 : 39.92
    Avg at pass 300 : 36.17
    Avg at pass 400 : 36.01
    Avg at pass 500 : 36.04
    Avg at pass 600 : 35.18
    Avg at pass 700 : 34.99
    Avg at pass 800 : 35.09
    Avg at pass 900 : 35.03
    Avg at pass 1000 : 35.02
    Avg at pass 1100 : 35.17
    Avg at pass 1200 : 35.07
    Avg at pass 1300 : 35.21
    Avg at pass 1400 : 35.03
    Avg at pass 1500 : 35.04
    Avg at pass 1600 : 35.37
    Avg at pass 1700 : 34.99
    Avg at pass 1800 : 35.1
    Avg at pass 1900 : 36.01
    Avg at pass 2000 : 36.02],

  [5],
  [30.15],
  [Avg at pass 100 : 31.2
    Avg at pass 200 : 29.65
    Avg at pass 300 : 29.65
    Avg at pass 400 : 29.52
    Avg at pass 500 : 29.65
    Avg at pass 600 : 29.67
    Avg at pass 700 : 29.66
    Avg at pass 800 : 29.58
    Avg at pass 900 : 29.61
    Avg at pass 1000 : 29.73
    Avg at pass 1100 : 28.64
    Avg at pass 1200 : 29.73
    Avg at pass 1300 : 32.29
    Avg at pass 1400 : 31.58
    Avg at pass 1500 : 30.09
    Avg at pass 1600 : 30.21
    Avg at pass 1700 : 30.41
    Avg at pass 1800 : 30.65
    Avg at pass 1900 : 30.53
    Avg at pass 2000 : 30.89],

  [6],
  [27.05],
  [Avg at pass 100 : 35.87
    Avg at pass 200 : 25.68
    Avg at pass 300 : 25.85
    Avg at pass 400 : 25.8
    Avg at pass 500 : 25.6
    Avg at pass 600 : 25.66
    Avg at pass 700 : 25.46
    Avg at pass 800 : 26.33
    Avg at pass 900 : 25.66
    Avg at pass 1000 : 25.59
    Avg at pass 1100 : 26.3
    Avg at pass 1200 : 25.8
    Avg at pass 1300 : 27.14
    Avg at pass 1400 : 27.63
    Avg at pass 1500 : 28.3
    Avg at pass 1600 : 27.19
    Avg at pass 1700 : 27.4
    Avg at pass 1800 : 27.33
    Avg at pass 1900 : 27.35
    Avg at pass 2000 : 29.04],

  [7],
  [30.00],
  [Avg at pass 100 : 34.8
    Avg at pass 200 : 30.24
    Avg at pass 300 : 29.27
    Avg at pass 400 : 28.99
    Avg at pass 500 : 29.43
    Avg at pass 600 : 29.21
    Avg at pass 700 : 28.99
    Avg at pass 800 : 28.45
    Avg at pass 900 : 29.33
    Avg at pass 1000 : 28.86
    Avg at pass 1100 : 28.49
    Avg at pass 1200 : 29.69
    Avg at pass 1300 : 29.91
    Avg at pass 1400 : 29.41
    Avg at pass 1500 : 28.39
    Avg at pass 1600 : 29.77
    Avg at pass 1700 : 32.72
    Avg at pass 1800 : 34.46
    Avg at pass 1900 : 29.62
    Avg at pass 2000 : 30.05],
)
== Efficienza

= Verifica formale

// #let x = lq.arange(1, 13)
// #let y1 = (1, 2.0030017196089256, 2.941905655637812, 3.8552529552504615, 4.668102211486331, 5.46988708020022)
// #let speedup = (
//   1,
//   1.983480794299447,
//   2.963325261839834,
//   3.926338168992499,
//   4.88345913517745,
//   5.821932356761625,
//   6.751883103519242,
//   7.663927411389719,
//   8.573884310284729,
//   9.431003739710809,
//   10.082615749276961,
//   6.8906698457918525,
// )
// #let strong_scaling_efficiency = (
//   1,
//   0.9917403971497235,
//   0.9877750872799447,
//   0.9815845422481247,
//   0.97669182703549,
//   0.9703220594602708,
//   0.9645547290741775,
//   0.9579909264237149,
//   0.9526538122538588,
//   0.9431003739710808,
//   0.916601431752451,
//   0.574222487149321,
// )

// #show: lq.set-tick(
//   shorten-sub: 100%,
// )

// #show lq.selector(lq.diagram): set align(center)

// #lq.diagram(
//   title: "Misura dello speedup",
//   legend: (position: top + left),
//   width: 6cm,
//   height: 6cm,
//   xlim: (1, 12),
//   ylim: (1, 12),
//   lq.plot(x, speedup, stroke: blue, mark: "s", label: "speedup"),
//   lq.line((1, 1), (12, 12), stroke: (paint: orange, dash: "dashed"), label: "p"),
//   lq.xaxis(label: [Number of processors p], ticks: x),
//   lq.yaxis(label: [Speedup S(p)]),
// )

// #lq.diagram(
//   title: "Misura della strong scaling efficiency",
//   legend: (position: bottom + right),
//   xlim: (1, 12),
//   ylim: (0, 1),
//   width: 6cm,
//   height: 6cm,
//   lq.plot(x, strong_scaling_efficiency, stroke: blue, mark: "s", label: "strong scaling efficiency"),
//   lq.xaxis(label: [Number of processors p], ticks: x),
//   lq.yaxis(label: [Strong scaling efficiency E(p)]),
// )

// Lo speedup aumenta come ci si aspetta fino ai 12 core, dove cala drasticamente a 7 quando ci si aspettava un valore vicino a 11. Questo calo di prestazioni, che si verifica anche negli altri test, non può attribuirsi esclusivamente alla porzione seriale del programma, in quanto è decisamente ridotta. Ipotizzo che il calo sia dovuto al fatto che, dal momento che tutti i processori del server, sia fisici che logici, sono occupati, il numero di context switch che coinvolgono il programma, cioè il numero di volte in cui il processo deve essere spostato da e verso un processore, complessivamente aumenti. Non escludo ci possano anche essere accessi non ottimali alla memoria dovuti all'architettura multi socket del server.

// Per completezza si includono gli speedup in tutti i test:

// #lq.diagram(
//   title: "Misura dello speedup (p=11)",
//   xaxis: (
//     ticks: ("test1", "test2", "test3", "test4", "test5", "test6", "test7", "worst")
//       .map(rotate.with(-45deg, reflow: true))
//       .map(align.with(right))
//       .enumerate(),
//     subticks: none,
//     label: "Test",
//   ),
//   lq.bar(
//     range(8),
//     (3.99, 9.09, 9.98, 9.89, 10.16, 12.88, 11.88, 10.08),
//   ),
//   yaxis: (
//     label: "Speedup S(p)",
//   ),
// )

// Nel primo test case lo speedup è ancora basso, probabilmente per il fatto che dato che pochi punti appartengono alla skyline la porzione seriale del programma è elevata. All'aumentare dei punti nello skyline e delle loro dimensioni, lo speedup aumenta in linea con quello che ci si aspetta. Nel test6 e test7 si ottiene addirittura uno speedup superlineare (ricordo che p=11), determinato probabilmente da un maggior accesso alla memoria cache.



// = Conclusioni

// Si riportano i tempi di esecuzione (in secondi) della versione seriale, della versione a memoria condivisa e della versione CUDA, i relativi speedup e il throughput della versione CUDA per tutti i test forniti:

// #table(
//   columns: 7,
//   table.header(
//     [Test], [Tempo seriale], [Tempo OMP (p=11)], [Speedup OMP], [Tempo CUDA], [Speedup CUDA], [Throughput CUDA]
//   ),
//   [test1-N100000-D3], [0.058], [0.014], [3.99], [0.728], [0.080], [41200464809],
//   [test2-N100000-D4], [5.69], [0.626], [9.09], [0.671], [8.48], [59654114260],
//   [test3-N100000-D10], [41.49], [4.16], [9.98], [1.07], [38.60], [93035242214],
//   [test4-N100000-D8], [25.95], [2.62], [9.89], [0.96], [27.08], [83521114463],
//   [test5-N100000-D20], [158.67], [15.61], [10.16], [1.01], [157.43], [198442351566],
//   [test6-N100000-D50], [226.73], [17.60], [12.88], [1.10], [205.47], [453116041106],
//   [test7-N100000-D200], [260.79], [21.94], [11.88], [1.12], [232.44], [1783279194091],
//   [worst-N100000-D10], [134.04], [13.29], [10.08], [0.764], [175.42], [130870674417],
// )

// Visti i risultati ottenuti, in linea con quanto atteso, e le considerazioni precedenti, concludo ritenendo che le strategie di parallelismo adottate siano scalabili e dimostrino la loro efficienza in presenza di un grande quantitativo di dati.
