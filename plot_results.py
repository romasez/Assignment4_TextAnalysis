"""Plot CSV output; requires Python 3 + matplotlib. Run from repository root."""
import csv
from pathlib import Path
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'plots'
OUT.mkdir(exist_ok=True)

def read(name):
    with (ROOT / 'results' / name).open(newline='') as f:
        return list(csv.DictReader(f))

def plot(data, algorithms, x_key, heading, output, case=None):
    fig, ax = plt.subplots(figsize=(8, 5))
    for algorithm in algorithms:
        subset = [r for r in data if r['algorithm'] == algorithm and (case is None or r.get('pattern_case') == case)]
        subset.sort(key=lambda r: int(r[x_key]))
        ax.plot([int(r[x_key]) for r in subset], [float(r['avg_ms']) for r in subset], marker='o', label=algorithm)
    ax.set_title(heading)
    ax.set_xlabel('Input length (characters)')
    ax.set_ylabel('Average execution time (ms, 5 runs)')
    ax.grid(alpha=.25)
    ax.legend()
    fig.tight_layout()
    fig.savefig(OUT / output, dpi=180)
    plt.close(fig)

plot(read('dp.csv'), ['LCS','Edit Distance'], 'input_size', 'Dynamic programming: execution time', 'dp_times.png')
for case in ('present', 'absent'):
    plot(read('matching.csv'), ['Naive','KMP','Rabin-Karp'], 'text_length',
         f'String matching: {case} pattern (16 characters)', f'matching_{case}.png', case)
plot(read('kasai.csv'), ['Kasai'], 'input_size', 'Kasai LCP: execution time (suffix array supplied)', 'kasai_times.png')
print('Generated 4 plots in plots/')
