package Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import Render.IsingRender;

import Dynamics.Algorithm;
import Model.*;

public abstract class Log {
	public static int sign = -1;
	public static int time = 0;// 1 sweep/time
	public static FileWriter W = null;
	public static File Path;
	private static boolean log = false; // ~500F/ms

	private static DataSet d;
	private static IsingRender R;
	private static int N = 400, N1, N2, steps = 500000, stepsplot = 1000;
	public static DataSet[] D = new DataSet[stepsplot];
	public static Plotter p = new Plotter(D);

	// public static EventBuffer buffer = new EventBuffer(50);

	public static void init(IsingRender r, int n, int n2) {
		if (!log) {
			R = r;
			N1 = n;
			N2 = n2;
			N = N1 * N2;
			String name = N1 * N2 + "-" + Hamiltonian.kT() + "-"
					+ Algorithm.String() + ".txt";
			try {
				if (W != null)
					W.close();
				Path = new File(name);
				if (Path.exists()) {
					Path.delete();
				}
				W = new FileWriter(Path, true);
				W.write("#" + N1 + "x" + N2 + '\n');
				W.write("#" + "J=" + Hamiltonian.J() + ";h=" + Hamiltonian.h()
						+ ";kT=" + Hamiltonian.kT() + '\n');
				W.write("#" + "t E M\n");
				time = 0;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.out.println("New Log: " + name);
			}
			log = true;
		} else {
			System.out.println("logging off");
			log = false;
		}
	}

	public static void log() {
		d = new DataSet(time, -Hamiltonian.E_nn, Hamiltonian.E_m);
		plot();
		if (log) {
			try {
				W.write(d.toString());
				if (time % 1024 == 0)
					W.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// auto();
		}
		time++;
	}

	private static void auto() {
		if (time == steps) {
			log = false;
			if (Hamiltonian.getkT() < 10) {
				R.HamiltonAdd(0, 0, 0.5);
				init(R, N1, N2);
			} else
				IsingRender.Stop();
			try {
				W.flush();
			} catch (IOException e) {
				// IDGAF
			}
			System.out.println("finished");
		}
	}

	private static void plot() {
		D[time % stepsplot] = d;
		if ((time + 1) % stepsplot == 0)
			if (time < 2 * stepsplot)
				p.start(D);
			else
				p.set(D);
	}

	/**
	 * Halley's Comment
	 */
	private static void checkTransition() {
		if (Math.signum(Hamiltonian.E_m) == -sign) {
			System.out.println("trans");
			sign = -sign;
			// S.buffer.event();
		}
	}
}
