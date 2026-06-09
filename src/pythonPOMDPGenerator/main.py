import tkinter as tk
from tkinter import ttk, filedialog

import random
from gui import TransitionEditor, ObservationEditor, transitions, observations, STATES, ACTIONS, normalize_distribution

OBSERVATIONS = [f"O{i}" for i in range(1, 28)]
START_PROBS = [0.125] * 8
OBS_PROB = 0.037

REWARDS = {
    "BT": [85, 84, 40, 39, 45, 30, 15, 10],
    "FC": [75, 78, 50, 28, 35, 20, 17, 8],
    "NC": [90, 81, 50, 35, 40, 25, 15, 12],
}

def export_pomdp(filename="output.pomdp"):
    with open(filename, "w") as f:
        f.write("discount: 0.9\n")
        f.write("values: reward\n")
        f.write("states: " + " ".join(STATES) + "\n")
        f.write("actions: " + " ".join(ACTIONS) + "\n")
        f.write("observations: " + " ".join(OBSERVATIONS) + "\n\n")

        f.write("start: " + " ".join(map(str, START_PROBS)) + "\n\n")

        for a in ACTIONS:
            for s in STATES:
                for t in STATES:
                    p = transitions[a][s][t]
                    f.write(f"T: {a} : {s} : {t} {p:.3f}\n")
        f.write("\n")

        for a in ACTIONS:
            for s in STATES:
                for o in OBSERVATIONS:
                    p = observations[a][s][o]
                    f.write(f"O: {a} : {s} : {o} {p:.3f}\n")

        f.write("\n")

        for a in ACTIONS:
            for i, s in enumerate(STATES):
                f.write(f"R: {a} : {s} : * : *\n")
                f.write(f"{REWARDS[a][i]:.1f}\n")


def randomize_transitions():
    for a in ACTIONS:
        for s in STATES:
            for t in STATES:
                transitions[a][s][t] = random.random()
            normalize_distribution(transitions[a][s])


def randomize_observations():
    for a in ACTIONS:
        for s in STATES:
            for o in OBSERVATIONS:
                observations[a][s][o] = random.random()
            normalize_distribution(observations[a][s])


def randomize_all():
    randomize_transitions()
    randomize_observations()

root = tk.Tk()
def save_pomdp():
    filename = filedialog.asksaveasfilename(
        title="save POMDP",
        defaultextension=".pomdp",
        filetypes=[("POMDP files", "*.pomdp")],
        initialfile="model.pomdp"
    )

    if not filename:
        return

    export_pomdp(filename)

def randomize_and_save_ten():
    folder = filedialog.askdirectory(title="chose folder for 10 POMDP")
    if not folder:
        return

    for i in range(10):
        randomize_all()
        export_pomdp(f"{folder}/LRDM_{i}.POMDP")


root.title("POMDP Editor")

notebook = ttk.Notebook(root)
notebook.pack(fill="both", expand=True, padx=10, pady=10)


tab_transitions = ttk.Frame(notebook)
TransitionEditor(tab_transitions).pack(anchor="nw", padx=10, pady=10)
notebook.add(tab_transitions, text="Transitions")


tab_observations = ttk.Frame(notebook)
ObservationEditor(tab_observations).pack(anchor="nw", padx=10, pady=10)
notebook.add(tab_observations, text="Observations")


ttk.Button(root, text="save POMDP", command=save_pomdp).pack(pady=10)


ttk.Button(root, text="generate 10 random POMDP", command=randomize_and_save_ten).pack(pady=5)


root.mainloop()
