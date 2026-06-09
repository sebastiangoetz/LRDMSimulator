import tkinter as tk
import math
from tkinter import ttk
def normalize_distribution(dist_dict):
    keys = list(dist_dict.keys())
    values = list(dist_dict.values())
    total = sum(values)
    n = len(values)

    if total <= 0:
        base = round(1.0 / n, 3)
        for k in keys[:-1]:
            dist_dict[k] = base
        dist_dict[keys[-1]] = round(1.0 - base * (n - 1), 3)
        return

    normed = [v / total for v in values]
    rounded = [math.floor(v * 1000) / 1000 for v in normed]
    remainder = 1.0 - sum(rounded)
    for k, v in zip(keys[:-1], rounded):
        dist_dict[k] = v

    dist_dict[keys[-1]] = remainder


STATES = [f"S{i}" for i in range(1, 9)]
ACTIONS = ["BT", "FC", "NC"]
OBSERVATIONS = [f"O{i}" for i in range(1, 28)]
observations = {
    a: {
        s: {o: 1 / len(OBSERVATIONS) for o in OBSERVATIONS}
        for s in STATES
    }
    for a in ACTIONS
}
for a in ACTIONS:
    for s in STATES:
        normalize_distribution(observations[a][s])
transitions = {
    a: {
        s: {t: 1 / 8 for t in STATES}
        for s in STATES
    }
    for a in ACTIONS
}
for a in ACTIONS:
    for s in STATES:
        normalize_distribution(transitions[a][s])


class TransitionEditor(tk.Frame):
    def __init__(self, parent):
        super().__init__(parent)

        self.open_line_ref = None

        combinations = [(a, s) for a in ACTIONS for s in STATES]

        for idx, (a, s) in enumerate(combinations):
            col = idx // 12
            row = idx % 12

            line = TransitionLine(
                self, a, s, self.on_line_clicked
            )
            line.grid(
                row=row,
                column=col,
                sticky="w",
                padx=10,
                pady=2
            )

    def on_line_clicked(self, line):
        if self.open_line_ref is line:
            line.close()
            self.open_line_ref = None
            return

        if self.open_line_ref:
            self.open_line_ref.close()

        line.open()
        self.open_line_ref = line


class TransitionLine(tk.Frame):
    def __init__(self, parent, action, state_from, click_callback):
        super().__init__(parent)

        self.action = action
        self.state_from = state_from
        self.click_callback = click_callback
        self.editor = None

        self.btn = ttk.Button(
            self,
            text=self.closed_text(),
            width=32,
            command=lambda: self.click_callback(self)
        )
        self.btn.grid(row=0, column=0, sticky="w")

    def closed_text(self):
        return f"▶  Action={self.action}  From={self.state_from}"

    def open_text(self):
        return f"▼  Action={self.action}  From={self.state_from}"

    def open(self):
        if self.editor:
            return

        self.btn.config(text=self.open_text())

        self.editor = SliderEditor(self, self.action, self.state_from)
        self.editor.grid(
            row=1,
            column=0,
            sticky="w",
            padx=20,
            pady=4
        )

    def close(self):
        if self.editor:
            self.editor.destroy()
            self.editor = None
            self.btn.config(text=self.closed_text())


class SliderEditor(tk.Frame):
    def __init__(self, parent, action, state_from):
        super().__init__(parent)

        self.action = action
        self.state_from = state_from
        self.sliders = {}

        for i, s_to in enumerate(STATES):
            ttk.Label(self, text=s_to).grid(row=i, column=0, sticky="w")

            slider = tk.Scale(
                self,
                from_=0,
                to=1,
                resolution=0.001,
                orient=tk.HORIZONTAL,
                length=200,
                command=self.update
            )
            slider.set(transitions[action][state_from][s_to])
            slider.grid(row=i, column=1)

            self.sliders[s_to] = slider

        self.sum_label = ttk.Label(self, text="")
        self.sum_label.grid(
            row=len(STATES),
            column=0,
            columnspan=2,
            sticky="w",
            pady=(6, 2)
        )

        ttk.Button(
            self,
            text="Normalize",
            command=self.normalize
        ).grid(
            row=len(STATES) + 1,
            column=0,
            columnspan=2,
            sticky="w"
        )

        self.update()

    def update(self, *_):
        total = 0.0
        for s_to, slider in self.sliders.items():
            val = slider.get()
            transitions[self.action][self.state_from][s_to] = val
            total += val

        self.sum_label.config(text=f"Summe: {total:.2f}")

        if abs(total - 1.0) <= 0.01:
            self.sum_label.config(foreground="green")
        else:
            self.sum_label.config(foreground="red")

    def normalize(self):
        sliders = list(self.sliders.values())
        n = len(sliders)

        values = [sl.get() for sl in sliders]
        total = sum(values)

        if total <= 0:
            base = round(1.0 / n, 3)
            for sl in sliders[:-1]:
                sl.set(base)

            rest = round(1.0 - base * (n - 1), 3)
            sliders[-1].set(rest)
            return

        normed = [v / total for v in values]

        rounded = [math.floor(v * 1000) / 1000 for v in normed]

        rest = round(1.0 - sum(rounded), 3)
        rounded.append(rest)

        for sl, v in zip(sliders, rounded):
            sl.set(max(0.0, min(1.0, v)))

        self.update()

class ObservationEditor(tk.Frame):
    def __init__(self, parent):
        super().__init__(parent)

        self.action = tk.StringVar(value=ACTIONS[0])
        self.state = tk.StringVar(value=STATES[0])

        ttk.Label(self, text="Action").grid(row=0, column=0, sticky="w")
        ttk.OptionMenu(
            self, self.action, ACTIONS[0], *ACTIONS,
            command=self.load
        ).grid(row=0, column=1, sticky="w")

        ttk.Label(self, text="State").grid(row=1, column=0, sticky="w")
        ttk.OptionMenu(
            self, self.state, STATES[0], *STATES,
            command=self.load
        ).grid(row=1, column=1, sticky="w")

        self.slider_frame = ttk.Frame(self)
        self.slider_frame.grid(row=2, column=0, columnspan=2, pady=10)

        self.sliders = {}

        cols = 3
        rows = 9

        idx = 0
        for col in range(cols):
            for row in range(rows):
                o = OBSERVATIONS[idx]

                ttk.Label(self.slider_frame, text=o).grid(
                    row=row,
                    column=col * 2,
                    sticky="w",
                    padx=5
                )

                slider = tk.Scale(
                    self.slider_frame,
                    from_=0,
                    to=1,
                    resolution=0.001,
                    orient=tk.HORIZONTAL,
                    length=200,
                    command=self.update_value
                )

                slider.grid(row=row, column=col * 2 + 1)

                self.sliders[o] = slider
                idx += 1

        self.sum_label = ttk.Label(self, text="")
        self.sum_label.grid(row=3, column=0, columnspan=2, sticky="w")

        ttk.Button(
            self,
            text="Normalize",
            command=self.normalize
        ).grid(row=4, column=0, columnspan=2, sticky="w")

        self.load()

    def load(self, *_):
        a = self.action.get()
        s = self.state.get()

        for o, slider in self.sliders.items():
            slider.set(observations[a][s][o])

        self.update_sum()

    def update_value(self, *_):
        a = self.action.get()
        s = self.state.get()

        for o, slider in self.sliders.items():
            observations[a][s][o] = slider.get()

        self.update_sum()

    def update_sum(self):
        a = self.action.get()
        s = self.state.get()

        total = sum(observations[a][s].values())
        self.sum_label.config(text=f"Summe: {total:.3f}")

        self.sum_label.config(
            foreground="green" if abs(total - 1.0) <= 0.01 else "red"
        )

    def normalize(self):
        a = self.action.get()
        s = self.state.get()

        obs = observations[a][s]
        keys = list(obs.keys())
        values = list(obs.values())
        total = sum(values)
        n = len(values)

        if total <= 0:
            base = round(1.0 / n, 3)
            for k in keys[:-1]:
                obs[k] = base
            obs[keys[-1]] = round(1.0 - base * (n - 1), 3)
        else:
            normed = [v / total for v in values]
            rounded = [math.floor(v * 1000) / 1000 for v in normed]
            rest = round(1.0 - sum(rounded), 3)

            if rest < 0:
                rest = 0.0

            rounded.append(rest)

            for k, v in zip(keys, rounded):
                obs[k] = v

        self.load()