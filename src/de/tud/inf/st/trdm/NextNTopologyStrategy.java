package de.tud.inf.st.trdm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import de.tud.inf.st.trdm.util.IDGenerator;

public class NextNTopologyStrategy implements TopologyStrategy {

	@Override
	public Set<Link> initNetwork(Network n, Properties props) {
		int numMirrors = n.getMirrors().size();
		int numLinks = n.getNumTargetLinksPerMirror();
		Set<Link> ret = new HashSet<>();

		for (int i = 0; i < numMirrors; i++) {
			for (int j = 1; j <= numLinks; j++) {
				// get a random mirror
				Mirror source = n.getMirrors().get(i);
				if (i + j >= n.getMirrors().size())
					continue;
				Mirror target = n.getMirrors().get(i + j);
				ret.add(new Link(IDGenerator.getInstance().getNextID(), source, target, 0, props));
			}
		}
		return ret;
	}

	@Override
	public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int sim_time) {
		List<Mirror> mirrors = n.getMirrors();
		int numTargetLinksPerMirror = n.getNumTargetLinksPerMirror();
		
		//first add the mirrors
		int numMirrorsToAdd = newMirrors;
		List<Mirror> mirrorsToAdd = new ArrayList<>();
		for (int i = 0; i < numMirrorsToAdd; i++) {
			mirrorsToAdd.add(new Mirror(IDGenerator.getInstance().getNextID(), sim_time, props));
		}
		// get last N mirrors to connect to the new mirrors (numTargetedLinksPerMirror)
		List<Mirror> lastMirrors = new ArrayList<>();
		for (int i = numTargetLinksPerMirror; i > 0; i--) {
			lastMirrors.add(mirrors.get(mirrors.size() - i));
		}
		mirrors.addAll(mirrorsToAdd);
		// add links from old mirrors
		for (int i = 0; i < lastMirrors.size(); i++) {
			Mirror source = lastMirrors.get(i);
			for (int j = 1; j <= numTargetLinksPerMirror; j++) {
				Mirror target;
				if (i + j < lastMirrors.size()) {
					target = lastMirrors.get(i + j);
				} else {
					target = mirrorsToAdd.get(i + j - lastMirrors.size());
				}
				Link l = new Link(IDGenerator.getInstance().getNextID(), source, target, sim_time, props);
				n.getLinks().add(l);
			}
		}

		// add links for new mirrors
		for (int i = 0; i < mirrorsToAdd.size(); i++) {
			for (int j = 1; j <= numTargetLinksPerMirror; j++) {
				if (i + j < mirrorsToAdd.size())
					n.getLinks().add(new Link(IDGenerator.getInstance().getNextID(), mirrorsToAdd.get(i),
							mirrorsToAdd.get(i + j), sim_time, props));
			}
		}

	}

	@Override
	public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int sim_time) {
		List<Mirror> mirrors = n.getMirrors();
		
		for (int i = 0; i < removeMirrors; i++) {
			Mirror m = mirrors.get(i);
			m.shutdown(sim_time);
			for (Link l : m.getLinks()) {
				l.shutdown();
			}
		}
		// note: the number of closed links can vary due to shared links between
	}
	
	@Override
	public int getNumTargetLinks(Network n) {
		int ret = n.getNumTargetMirrors() * n.getNumTargetLinksPerMirror();
		for(int i = 1; i <= n.getNumTargetLinksPerMirror(); i++)
			ret -= i;
		return ret;
	}

}
