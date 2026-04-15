#import "@preview/lilaq:0.4.0" as lq

#align(center, text(18pt)[*Assignment 1 di Programmazione Concorrente e Distribuita 2025/2026*])
#align(center, text(12pt)[Mattia Ronchi, matr. 0001236997 \ Samorì Andrea 0001235969 \ Andrea Monaco 000])

= Analisi del problema

L'assignment ha l'obiettivo di realizzare una versione concorrente del gioco `Poool` fornito in 2 varianti:
1. Variante multi-threading, utilizzando i default/platform threads
2. Variante task-based, utilizzando il Java Executor Framework

= Aspetti rilevanti per la concorrenza

La logica principale del programma consiste nell'aggiornare lo stato del sistema, composto da un insieme di palle (giocatore e CPU) e palline, a seguito delle collisioni tra esse. Dato il costo quadratico ($O(n^2)$) del calcolo delle collisioni tra $n$ palline e dato che $n$ può aumentare significativamente (vedi SmallBoard vs MassiveBoard), la parte computazionalmente più dispendiosa consiste nel calcolare le collisioni tra queste ed è quella su cui si siamo concentrati. La realizzazione di una versione concorrente di questo calcolo deve prestare attenzione a possibili accessi simultanei a una singola pallina da parte di entità computazionali distinte.

= Design della soluzione

== Definizione dei task

Il task principale è la risoluzione delle collisioni tra tutte le coppie di palline. La seguente è la porzione di codice sequenziale che abbiamo intenzione di rendere concorrente:

```java
for (int i = 0; i < balls.size() - 1; i++) {
  for (int j = i + 1; j < balls.size(); j++) {
      Ball.resolveCollision(balls.get(i), balls.get(j));
  }
}
```

La risoluzione della collisione opera su due proprietà di ogni palla: posizione e velocità.

(eventuale aggiunta: applyCollision concorrente)

== Architettura

L'architettura concorrente è di tipo Master-Worker e utilizza diverse entità per coordinazione e per l'aggregazione dei risultati.

I componenti attivi sono il master e i worker. All'interno del model, al momento del calcolo delle collisioni tra le coppie di palline, il master crea i worker e assegna loro il lavoro.

I componenti passivi sono:
- latch per la sincronizzazione dei worker
- monitor presenti all'interno di ogni pallina per l'aggregazione dei risultati

// Abbiamo utilizzato il pattern architetturale MVC in combinazione con il pattern Observer. Il controller è un listener sugli eventi di input/output, mentre la view è un observer dell'aggiornamento dello stato della board (model).

== Variante multi-threading

== Variante task-based

= Performance

== Speedup

== Efficienza (da cambiare)

= Verifica formale

// #let x = lq.arange(1, 13)
// // #let y1 = (1, 2.0030017196089256, 2.941905655637812, 3.8552529552504615, 4.668102211486331, 5.46988708020022)
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
