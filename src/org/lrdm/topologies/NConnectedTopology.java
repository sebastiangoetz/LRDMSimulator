package org.lrdm.topologies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lrdm.Link;
import org.lrdm.Mirror;
import org.lrdm.Network;
import org.lrdm.effectors.Action;
import org.lrdm.effectors.MirrorChange;
import org.lrdm.effectors.TargetLinkChange;
import org.lrdm.util.IDGenerator;

import java.util.*;

/**
 * A {@link TopologyStrategy} which connects each mirror with exactly n other mirrors.
 * If n is the number of all mirrors - 1 this strategy equals the {@link FullyConnectedTopology}.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class NConnectedTopology extends TopologyStrategy {
    private static final Log log = LogFactory.getLog(NConnectedTopology.class);
    private static int max = -1;
    private static int min = Integer.MAX_VALUE;

    /**
     * Returns the next mirror from the network except for the mirror passed as self.
     * In addition, already connected mirrors are excluded.
     *
     * @param self The {@link Mirror} not to select.
     * @param n    The {@link Network}
     * @return The next {@link Mirror}
     */
    public Mirror getTargetMirror(Mirror self, Network n) {
        Mirror targetMirror = null;
        List<Mirror> options = new ArrayList<>(n.getMirrorsSortedById());
        boolean foundMirror = false;
        while (!options.isEmpty()) {
            targetMirror = null;
            for (Mirror t : options) {
                if (t.getID() > self.getID()) {
                    targetMirror = t;
                    options.remove(t);
                    foundMirror = true;
                    break;
                }
            }
            if (targetMirror == null) {
                targetMirror = options.remove(0);
                foundMirror = true;
            }
            if (targetMirror != self && !self.isLinkedWith(targetMirror)) break;
        }
        if (!foundMirror || targetMirror == self) targetMirror = null;
        return targetMirror;
    }

    /**
     * Initializes the network by connecting all mirrors to one another.
     *
     * @param n     the {@link Network}
     * @param props {@link Properties} of the simulation
     * @return {@link Set} of all {@link Link}s created
     */
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {
        Set<Link> ret = new HashSet<>();
        for (Mirror start : n.getMirrors()) {
            connectToOtherMirrors(n, props, start, ret);
        }
        return ret;
    }

    public static void newConnectToMirrors(Network n, Properties props, Set<Link> ret) {
        LinkedHashMap<Mirror, Boolean> firstMap = new LinkedHashMap<>();
        LinkedHashMap<Mirror, Boolean> secondMap = new LinkedHashMap<>();

        boolean flag = true;
        for (Mirror mirrors : n.getMirrorsSortedById()) {
            if (mirrors.getID() < n.getMirrorsSortedById().size() / 2) {
                firstMap.put(mirrors, flag);
            } else {
                secondMap.put(mirrors, flag);
            }
            flag = !flag;
        }
        secondMap = reverseMap(secondMap);
        for (int i = 0; i < 3; i++) {
            iterateInterleaved(firstMap, secondMap, n);
        }
        checkFalseLinksCount(firstMap, secondMap, n, props, ret);
    }

    private static void checkFalseLinksCount(Map<Mirror, Boolean> first, Map<Mirror, Boolean> second, Network network, Properties props, Set<Link> ret) {
//        1. check the difference between needed lpms count of each mirror and current lpms count
//        2. in cycle for each point of difference: choose two links of the current mirror, but they do not have connection to each other
//                      --> remove these two links (from current to target links) --> connect two target links
//        3. make checks till each mirror has equal amount of lpm

        List<Mirror> mirrors = network.getMirrorsSortedById();
        for (Mirror mirror : mirrors) {
            if (mirror.getLinks().size() > network.getNumTargetLinksPerMirror()) {
                List<Integer> mirrorsForNewLink = fixLinksCount(mirror, network).stream().toList();
                if (!mirrorsForNewLink.isEmpty()) {


                    Mirror mirrorSource = network.getMirrorsSortedById().stream().filter(m -> m.getID() == mirrorsForNewLink.get(0)).toList().get(0);
                    //network.getMirrorsSortedById().get(mirrorsForNewLink.get(0));
                    Mirror mirrorTarget = network.getMirrorsSortedById().stream().filter(m -> m.getID() == mirrorsForNewLink.get(1)).toList().get(0);
//                     network.getMirrorsSortedById().get(mirrorsForNewLink.get(1));


                    Link linkToAdd = new Link(IDGenerator.getInstance().getNextID(), mirrorSource, mirrorTarget, 0, props);
                    mirrorSource.addLink(linkToAdd);
                    mirrorTarget.addLink(linkToAdd);
                    ret.add(linkToAdd);
                }
            }
        }
    }

    private static Set<Integer> fixLinksCount(Mirror mirror, Network network) {
        List<Link> links = new ArrayList<>(mirror.getLinks());
        List<Link> linksToRemove = new ArrayList<>();
        Set<Integer> mirrorIds = new HashSet<>();

        for (int i = 0; i < links.size(); i++) {
            for (int j = 0; j < links.size(); j++) {
                if (links.get(i).getID() != links.get(j).getID()) {
                    if (links.get(i).getTarget().getID() == mirror.getID()) {
//                        source and target / source and source
                        if (links.get(j).getTarget().getID() == mirror.getID()) {
                            Link linkToSave = findLinkWithSourceAndTarget(network.getLinks(), links.get(i).getSource().getID(), links.get(j).getSource().getID());
                            if (linkToSave == null) {
                                linksToRemove.add(links.get(i));
                                linksToRemove.add(links.get(j));
                                mirrorIds.add(links.get(i).getSource().getID());
                                mirrorIds.add(links.get(j).getSource().getID());
                                break;
                            }
                        } else {
                            Link linkToSave = findLinkWithSourceAndTarget(network.getLinks(), links.get(i).getSource().getID(), links.get(j).getTarget().getID());
                            if (linkToSave == null) {
                                linksToRemove.add(links.get(i));
                                linksToRemove.add(links.get(j));
                                mirrorIds.add(links.get(i).getSource().getID());
                                mirrorIds.add(links.get(j).getTarget().getID());
                                break;
                            }
                        }
                    } else {
//                        target and source / target and target
                        if (links.get(j).getTarget().getID() == mirror.getID()) {
                            Link linkToSave = findLinkWithSourceAndTarget(network.getLinks(), links.get(i).getTarget().getID(), links.get(j).getSource().getID());
                            if (linkToSave == null) {
                                linksToRemove.add(links.get(i));
                                linksToRemove.add(links.get(j));
                                mirrorIds.add(links.get(i).getTarget().getID());
                                mirrorIds.add(links.get(j).getSource().getID());
                                break;
                            }
                        } else {
                            Link linkToSave = findLinkWithSourceAndTarget(network.getLinks(), links.get(i).getTarget().getID(), links.get(j).getTarget().getID());
                            if (linkToSave == null) {
                                linksToRemove.add(links.get(i));
                                linksToRemove.add(links.get(j));
                                mirrorIds.add(links.get(i).getTarget().getID());
                                mirrorIds.add(links.get(j).getTarget().getID());
                                break;
                            }
                        }
                    }
                }
            }
            if (!linksToRemove.isEmpty()) {
                break;
            }
        }
        if (!linksToRemove.isEmpty()) {
            mirror.getLinks().removeAll(linksToRemove);
            linksToRemove.forEach(it -> {
                network.getMirrorsSortedById().stream().filter(m -> m.getID() == it.getSource().getID()).toList().get(0).getLinks().remove(it);
                network.getMirrorsSortedById().stream().filter(m -> m.getID() == it.getTarget().getID()).toList().get(0).getLinks().remove(it);


//                network.getMirrorsSortedById().get(it.getSource().getID()).getLinks().remove(it);
//                network.getMirrorsSortedById().get(it.getTarget().getID()).getLinks().remove(it);
            });
            linksToRemove.forEach(network.getLinks()::remove);
        }
        return mirrorIds;
    }

    private static Link findLinkWithSourceAndTarget(Set<Link> linkSet, int sourceId, int targetId) {
        for (Link link : linkSet) {
            if (link.getSource().getID() == sourceId && link.getTarget().getID() == targetId) {
                return link;
            } else if (link.getTarget().getID() == sourceId && link.getSource().getID() == targetId) {
                return link;
            }
        }
        return null;
    }


    private static <K, V> LinkedHashMap<K, V> reverseMap(LinkedHashMap<K, V> map) {
        LinkedHashMap<K, V> reversedMap = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
        Collections.reverse(entryList);

        for (Map.Entry<K, V> entry : entryList) {
            reversedMap.put(entry.getKey(), entry.getValue());
        }

        return reversedMap;
    }

    //remove if the current link is min or max, but if it also has more than required lpm
    public static void iterateInterleaved(Map<Mirror, Boolean> first, Map<Mirror, Boolean> second, Network n) {
        Iterator<Map.Entry<Mirror, Boolean>> firstIterator = first.entrySet().iterator();
        Iterator<Map.Entry<Mirror, Boolean>> secondIterator = second.entrySet().iterator();

        while (firstIterator.hasNext() || secondIterator.hasNext()) {
            if (firstIterator.hasNext()) {
                deleteLinkByIteration(firstIterator, n);
            }
            if (secondIterator.hasNext()) {
                deleteLinkByIteration(secondIterator, n);
            }
        }


    }

    private static void deleteLinkByIteration(Iterator<Map.Entry<Mirror, Boolean>> iterator, Network network) {
        Map.Entry<Mirror, Boolean> entry = iterator.next();
        Set<Link> links = entry.getKey().getLinks();
        if (links.size() > network.getNumTargetLinksPerMirror()) {
            if (entry.getValue()) {
                max = -1;
                Link linkToDelete = findMaxLinkToDelete(links, entry, network);
                deleteLinkFully(linkToDelete, network, links, max);
                max = -1;
            } else {
                min = network.getNumMirrors();
                Link linkToDelete = findMinLinkToDelete(links, entry, network);
                deleteLinkFully(linkToDelete, network, links, min);
                min = network.getNumMirrors();
            }
        }
    }

    private static Link findMaxLinkToDelete(Set<Link> links, Map.Entry<Mirror, Boolean> entry, Network network) {
        Link linkToDelete = null;
        for (Link link : links) {
            if (link.getTarget().getID() == entry.getKey().getID()) {
                if (link.getSource().getID() > max && link.getSource().getLinks().size() > network.getNumTargetLinksPerMirror()) {
                    max = link.getSource().getID();
                    linkToDelete = link;
                }
            } else {
                if (link.getTarget().getID() > max && link.getTarget().getLinks().size() > network.getNumTargetLinksPerMirror()) {
                    max = link.getTarget().getID();
                    linkToDelete = link;
                }
            }
        }
        return linkToDelete;
    }

    private static Link findMinLinkToDelete(Set<Link> links, Map.Entry<Mirror, Boolean> entry, Network network) {
        Link linkToDelete = null;
        for (Link link : links) {
            if (link.getTarget().getID() == entry.getKey().getID()) {
                if (link.getSource().getID() < min && link.getSource().getLinks().size() > network.getNumTargetLinksPerMirror()) {
                    min = link.getSource().getID();
                    linkToDelete = link;
                }
            } else {
                if (link.getTarget().getID() < min && link.getTarget().getLinks().size() > network.getNumTargetLinksPerMirror()) {
                    min = link.getTarget().getID();
                    linkToDelete = link;
                }
            }
        }
        return linkToDelete;
    }

    private static void deleteLinkFully(Link linkToDelete, Network network, Set<Link> links, int mirrorId) {
        if (linkToDelete != null) {
            network.getLinks().remove(linkToDelete);
            for (Iterator<Link> iterator = links.iterator(); iterator.hasNext(); ) {
                Link link = iterator.next();
                if (link == linkToDelete) {
                    iterator.remove();
                }
            }
            deleteLinkFromTarget(network, mirrorId, linkToDelete);
        }
    }

    private static void deleteLinkFromTarget(Network network, int mirrorId, Link linkToDelete) {
        Mirror currentMirror = network.getMirrorsSortedById().stream().filter(mirror -> mirror.getID() == mirrorId).toList().get(0);
        for (Iterator<Link> iterator = /*network.getMirrorsSortedById().get(mirrorId)*/currentMirror.getLinks().iterator(); iterator.hasNext(); ) {
            Link link = iterator.next();
            if (link == linkToDelete) {
                iterator.remove();
            }
        }
    }

    private void connectToOtherMirrors(Network n, Properties props, Mirror start, Set<Link> ret) {
        for (int i = 0; i < n.getNumTargetLinksPerMirror(); i++) {
            Mirror targetMirror = getTargetMirror(start, n);
            if (targetMirror == null) continue;
            Link l = new Link(IDGenerator.getInstance().getNextID(), start, targetMirror, 0, props);
            start.addLink(l);
            targetMirror.addLink(l);
            ret.add(l);
        }
    }

    private void connectNewMirrorsToOther(Network n, Properties props, Mirror start, Set<Link> ret) {
//            1. add new mirror
//            2. find lpm/2 pairs of mirrors that are connected to each other
//            3. remove these links
//            4. add links from new mirror to sources and targets of deleted links

//        for even old mirrors count
//                * before one mirror had one link less --> add one more link to this mirror from new one
//        for odd old mirrors count
//                * new mirror has one links less than other has

        Set<Link> linksToAdd = new HashSet<>();
        if (!isEven(n.getNumMirrors()) && !isEven(n.getNumTargetLinksPerMirror())) {
            for (int i = 0; i < n.getMirrors().size(); i++) {
                if (n.getMirrors().get(i).getLinks().size() < n.getNumTargetLinksPerMirror()) {
                    Link linkToAdd = new Link(IDGenerator.getInstance().getNextID(), start, n.getMirrors().get(i), 0, props);
                    start.addLink(linkToAdd);
                    n.getMirrors().get(i).addLink(linkToAdd);
                    if (linkToAdd.getTarget().getID() == linkToAdd.getSource().getID())
                        System.out.println("here");
                    linksToAdd.add(linkToAdd);
                    break;
                }
            }
        }
        for (int i = 0; i < n.getNumTargetLinksPerMirror() / 2; i++) {
//        add check if(this link is not from the mirror that has one link less than other
            Random rand = new Random();
            int currentIndex = rand.nextInt(n.getMirrors().size() - 1); // maybe -2
            if (!n.getMirrorsSortedById().stream().filter(m -> m.getID() == currentIndex).toList().isEmpty() &&
                    !n.getMirrorsSortedById().stream().filter(m -> m.getID() == currentIndex).toList().get(0).getLinks().isEmpty()) {
                List<Link> linksToDelete =
                        n.getMirrorsSortedById().
                                stream().
                                filter(m -> m.getID() == currentIndex).toList().get(0).
                                getLinks().
                                stream().
                                toList();


                Link linkToDelete = linksToDelete.stream().
                        filter(mirror -> mirror.getSource().getID() != start.getID()
                                && mirror.getTarget().getID() != start.getID()).
                        toList().stream().findFirst().orElse(null);

                if (linkToDelete != null) {


//            Link linkToDelete = n.getMirrorsSortedById().get(currentIndex).getLinks().stream().toList().stream().filter(mirror -> mirror.getSource().getID() != start.getID() && mirror.getTarget().getID() != start.getID()).toList().get(0);
                    Mirror firstTargetMirror = linkToDelete.getTarget();
                    Mirror secondTargetMirror = linkToDelete.getSource();
                    Link firstLinkToAdd = new Link(IDGenerator.getInstance().getNextID(), start, firstTargetMirror, 0, props);
                    Link secondLinkToAdd = new Link(IDGenerator.getInstance().getNextID(), start, secondTargetMirror, 0, props);
//               remove old
                    firstTargetMirror.removeLink(linkToDelete);
                    secondTargetMirror.removeLink(linkToDelete);
                    n.getLinks().remove(linkToDelete);
//                add new
                    start.addLink(firstLinkToAdd);
                    firstTargetMirror.addLink(firstLinkToAdd);
                    start.addLink(secondLinkToAdd);
                    secondTargetMirror.addLink(secondLinkToAdd);

                    if (firstLinkToAdd.getTarget().getID() == firstLinkToAdd.getSource().getID()) {
                        System.out.println("here");
                    }

                    if (secondLinkToAdd.getTarget().getID() == secondLinkToAdd.getSource().getID()) {
                        System.out.println("here");
                    }
                    linksToAdd.add(firstLinkToAdd);
                    linksToAdd.add(secondLinkToAdd);
                }
            }
        }

        List<Link> links = new ArrayList<>(linksToAdd.stream().toList());
        links.removeIf(link -> link.getSource().getID() == link.getTarget().getID());
        ret.addAll(links);
    }

    private static void deleteMirror(Mirror mirror, Network network) {
        List<Link> linksToRemove = network.getLinks().stream().toList();

        for (Link link : linksToRemove) {
            if (link.getSource() == mirror) {
                Mirror targetMirror = link.getTarget();
                Mirror sourceMirror = link.getSource();
                targetMirror.removeLink(link);
                sourceMirror.removeLink(link);
                network.getLinks().remove(link);
            } else if (link.getTarget() == mirror) {
                Mirror targetMirror = link.getTarget();
                Mirror sourceMirror = link.getSource();
                targetMirror.removeLink(link);
                sourceMirror.removeLink(link);
                network.getLinks().remove(link);
            }
        }
        network.getMirrors().remove(mirror);
    }

    private static boolean isEven(int number) {
        return number % 2 == 0;
    }

    /**
     * Closes all current links and creates new links between all mirrors.
     *
     * @param n       the {@link Network}
     * @param props   {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void restartNetwork(Network n, Properties props, int simTime) {
        super.restartNetwork(n, props, simTime);
        Set<Link> ret = new HashSet<>();
        for (Mirror m : n.getMirrors()) {
            connectToOtherMirrors(n, props, m, ret);
        }
        n.getLinks().addAll(ret);
    }

    @Override
    public void modifyNetworkAfterAddLink(Network network, Properties props, int timeStep) {
//        odd mirrors and from even to odd lpms --> one mirror will have one link less
//        odd to even lpms --> each mirror will have equal count of lpms

//        divide into two maps as before with bool min max check one from each side and add link where needed
//        at the end check if each mirror has equal count of links

        LinkedHashMap<Mirror, Boolean> firstMap = new LinkedHashMap<>();
        LinkedHashMap<Mirror, Boolean> secondMap = new LinkedHashMap<>();

        boolean flag = true;
        for (int i = 0; i < network.getMirrorsSortedById().size(); i++) {
            if (i < network.getMirrorsSortedById().size() / 2) {
                firstMap.put(network.getMirrorsSortedById().get(i), flag);
            } else {
                secondMap.put(network.getMirrorsSortedById().get(i), flag);
            }
            flag = !flag;
        }
        secondMap = reverseMap(secondMap);

        for (int i = 0; i < 3; i++) {
            addLink(firstMap, secondMap, network);
        }

        List<Link> links = new ArrayList<>(network.getLinks().stream().toList());
        links.removeIf(link -> link.getSource().getID() == link.getTarget().getID());
    }

    private void addLink(Map<Mirror, Boolean> firstMap, Map<Mirror, Boolean> secondMap, Network network) {
        Iterator<Map.Entry<Mirror, Boolean>> firstIterator = firstMap.entrySet().iterator();
        Iterator<Map.Entry<Mirror, Boolean>> secondIterator = secondMap.entrySet().iterator();

        while (firstIterator.hasNext() || secondIterator.hasNext()) {
            if (firstIterator.hasNext()) {
                Map.Entry<Mirror, Boolean> entry = firstIterator.next();
                Mirror source = entry.getKey();
                if (entry.getValue()) {

                    max = -1;
                    Mirror target = null;
                    for (Mirror mirror2 : network.getMirrorsSortedById()) {
                        if (mirror2.getID() > max && !source.isLinkedWith(mirror2) && mirror2.getLinks().size() < network.getNumTargetLinksPerMirror()) {
                            max = mirror2.getID();
                            target = mirror2;
                        }
                    }

                    if (target != null) {
                        Link linkToAdd = new Link(IDGenerator.getInstance().getNextID(), source, target, 0, network.getProps());
                        source.addLink(linkToAdd);
                        target.addLink(linkToAdd);
                        network.getLinks().add(linkToAdd);
                    }
                    max = -1;
                } else {
                    min = Integer.MAX_VALUE;
                    Mirror target = null;
                    for (Mirror mirror2 : network.getMirrorsSortedById()) {
                        if (mirror2.getID() < min && !source.isLinkedWith(mirror2) && mirror2.getLinks().size() < network.getNumTargetLinksPerMirror()) {
                            min = mirror2.getID();
                            target = mirror2;
                        }
                    }

                    if (target != null) {
                        Link linkToAdd = new Link(IDGenerator.getInstance().getNextID(), source, target, 0, network.getProps());
                        source.addLink(linkToAdd);
                        target.addLink(linkToAdd);
                        network.getLinks().add(linkToAdd);
                    }
                    min = Integer.MAX_VALUE;
                }

            }
            if (secondIterator.hasNext()) {
                Map.Entry<Mirror, Boolean> entry = secondIterator.next();
                Mirror source = entry.getKey();

                if (entry.getValue()) {
                    max = -1;
                    Mirror target = null;
                    for (Mirror mirror2 : network.getMirrorsSortedById()) {
                        if (mirror2.getID() > max && !source.isLinkedWith(mirror2) && mirror2.getLinks().size() < network.getNumTargetLinksPerMirror()) {
                            max = mirror2.getID();
                            target = mirror2;
                        }
                    }
                    if (target != null) {
                        Link linkToAdd = new Link(IDGenerator.getInstance().getNextID(), source, target, 0, network.getProps());
                        source.addLink(linkToAdd);
                        target.addLink(linkToAdd);
                        network.getLinks().add(linkToAdd);
                    }
                    max = -1;
                } else {
                    min = Integer.MAX_VALUE;
                    Mirror target = null;
                    for (Mirror mirror2 : network.getMirrorsSortedById()) {
                        if (mirror2.getID() < min && !source.isLinkedWith(mirror2) && mirror2.getLinks().size() < network.getNumTargetLinksPerMirror()) {
                            min = mirror2.getID();
                            target = mirror2;
                        }
                    }

                    if (target != null) {
                        Link linkToAdd = new Link(IDGenerator.getInstance().getNextID(), source, target, 0, network.getProps());
                        source.addLink(linkToAdd);
                        target.addLink(linkToAdd);
                        network.getLinks().add(linkToAdd);
                    }
                    min = Integer.MAX_VALUE;
                }
            }
        }
    }

    @Override
    public void modifyNetworkAfterLinkRemove(Network n, Properties props, int simTime) {
//        by moving from 3 to 2 lpms there will be two mirrors with 3 links, which need to be modified extra after the main algorithm

//        divide into two maps with min max -> remove link if possible

        LinkedHashMap<Mirror, Boolean> firstMap = new LinkedHashMap<>();
        LinkedHashMap<Mirror, Boolean> secondMap = new LinkedHashMap<>();

        boolean flag = true;
        for (int i = 0; i < n.getMirrorsSortedById().size(); i++) {
            if (i < n.getMirrorsSortedById().size() / 2) {
                firstMap.put(n.getMirrorsSortedById().get(i), flag);
            } else {
                secondMap.put(n.getMirrorsSortedById().get(i), flag);
            }
            flag = !flag;
        }
        secondMap = reverseMap(secondMap);

        removeLink(firstMap, secondMap, n);

          Link last = null;
    }

    private void removeLink(Map<Mirror, Boolean> firstMap, Map<Mirror, Boolean> secondMap, Network n) {
        Iterator<Map.Entry<Mirror, Boolean>> firstIterator = firstMap.entrySet().iterator();
        Iterator<Map.Entry<Mirror, Boolean>> secondIterator = secondMap.entrySet().iterator();

        while (firstIterator.hasNext() || secondIterator.hasNext()) {
            if (firstIterator.hasNext()) {
                Map.Entry<Mirror, Boolean> entry = firstIterator.next();
                Mirror source = entry.getKey();
                if (entry.getValue()) {

                    max = -1;

                    Link linkToDelete = null;
                    for (Link link : source.getLinks()) {
                        if (link.getTarget().getID() == source.getID()) {
                            if (link.getSource().getID() > max && link.getSource().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                max = link.getSource().getID();
                                linkToDelete = link;
                            }
                        } else if (link.getSource().getID() == source.getID()) {
                            if (link.getTarget().getID() > max && link.getTarget().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                max = link.getTarget().getID();
                                linkToDelete = link;
                            }
                        }
                    }


                    if (linkToDelete != null) {
                        for (Iterator<Link> iterator = linkToDelete.getSource().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        for (Iterator<Link> iterator = linkToDelete.getTarget().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        n.getLinks().remove(linkToDelete);
                    }
                    max = -1;
                } else {
                    min = Integer.MAX_VALUE;

                    Link linkToDelete = null;
                    for (Link link : source.getLinks()) {
                        if (link.getTarget().getID() == source.getID()) {
                            if (link.getSource().getID() < min && link.getSource().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                min = link.getSource().getID();
                                linkToDelete = link;
                            }
                        } else if (link.getSource().getID() == source.getID()) {
                            if (link.getTarget().getID() < min && link.getTarget().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                min = link.getTarget().getID();
                                linkToDelete = link;
                            }
                        }
                    }
                    if (linkToDelete != null) {
                        for (Iterator<Link> iterator = linkToDelete.getSource().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        for (Iterator<Link> iterator = linkToDelete.getTarget().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        n.getLinks().remove(linkToDelete);
                    }

                    min = Integer.MAX_VALUE;
                }

            }
            if (secondIterator.hasNext()) {
                Map.Entry<Mirror, Boolean> entry = secondIterator.next();
                Mirror source = entry.getKey();

                if (entry.getValue()) {
                    max = -1;
                    Link linkToDelete = null;
                    for (Link link : source.getLinks()) {
                        if (link.getTarget().getID() == source.getID()) {
                            if (link.getSource().getID() > max && link.getSource().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                max = link.getSource().getID();
                                linkToDelete = link;
                            }
                        } else if (link.getSource().getID() == source.getID()) {
                            if (link.getTarget().getID() > max && link.getTarget().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                max = link.getTarget().getID();
                                linkToDelete = link;
                            }
                        }
                    }
                    if (linkToDelete != null) {
                        for (Iterator<Link> iterator = linkToDelete.getSource().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        for (Iterator<Link> iterator = linkToDelete.getTarget().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        n.getLinks().remove(linkToDelete);
                    }

                    max = -1;
                } else {
                    min = Integer.MAX_VALUE;
                    Link linkToDelete = null;
                    for (Link link : source.getLinks()) {
                        if (link.getTarget().getID() == source.getID()) {
                            if (link.getSource().getID() < min && link.getSource().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                min = link.getSource().getID();
                                linkToDelete = link;
                            }
                        } else if (link.getSource().getID() == source.getID()) {
                            if (link.getTarget().getID() < min && link.getTarget().getLinks().size() > n.getNumTargetLinksPerMirror()) {
                                min = link.getTarget().getID();
                                linkToDelete = link;
                            }
                        }
                    }
                    if (linkToDelete != null) {
                        for (Iterator<Link> iterator = linkToDelete.getSource().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        for (Iterator<Link> iterator = linkToDelete.getTarget().getLinks().iterator(); iterator.hasNext(); ) {
                            Link link = iterator.next();
                            if (link == linkToDelete) {
                                iterator.remove();
                            }
                        }
                        n.getLinks().remove(linkToDelete);
                    }


                    min = Integer.MAX_VALUE;
                }
            }
        }

        if (!isEven(n.getMirrorsSortedById().size()) && n.getNumTargetLinksPerMirror() == 2) {
            List<Mirror> m = n.getMirrorsSortedById().stream().filter(it -> it.getLinks().size() > n.getNumTargetLinksPerMirror()).toList();
            if (!m.isEmpty()) {
                System.out.println("THERE IS MORE THAN 2 LPM");
            }

        }
    }

    @Override
    public void modifyNetworkAfterRemoveMirror(Network n, int removeMirrors, Properties props, int simTime) {

        //if even -> odd mirrors with even links: find pairs that are not connected and connect them (if there are no such a pairs --> modify network)
        //if odd -> even mirrors with even links: same
        //if odd -> even mirrors with odd links: REMOVE MIRROR THAT HAS ONE LINK LESS, and then same
        //if even -> odd mirrors with odd links: same, but one will stay with one less link
        for (int i = 0; i < removeMirrors; i++) {
            if (!isEven(n.getMirrors().size()) && !isEven(n.getNumTargetLinks())) {
                if (!n.getMirrors().stream().filter(it -> it.getLinks().size() < n.getNumTargetLinksPerMirror()).toList().isEmpty()) {
                    Mirror m = n.getMirrors().stream().filter(it -> it.getLinks().size() < n.getNumTargetLinksPerMirror()).toList().get(0);
                    deleteMirror(m, n);
                } else {
                    deleteMirror(n.getMirrorsSortedById().get(n.getMirrorsSortedById().size() - 1), n);
                }
            } else {
                deleteMirror(n.getMirrorsSortedById().get(n.getMirrorsSortedById().size() - 1), n);
            }

            for (int j = 0; j < n.getNumTargetLinksPerMirror() / 2; j++) {
                List<Mirror> mirrors = n.getMirrorsSortedById().stream().toList().stream().filter(it -> it.getLinks().size() < n.getNumTargetLinksPerMirror()).toList();
                for (int k = 0; k < mirrors.size(); k++) {
                    if (mirrors.get(0).getID() != mirrors.get(k).getID()) {
                        Link linkToSave = findLinkWithSourceAndTarget(n.getLinks(), mirrors.get(0).getID(), mirrors.get(k).getID());
                        if (linkToSave == null) {
                            linkToSave = new Link(IDGenerator.getInstance().getNextID(), mirrors.get(0), mirrors.get(k), 0, props);
                            mirrors.get(k).addLink(linkToSave);
                            mirrors.get(0).addLink(linkToSave);
                            n.getLinks().add(linkToSave);
                            break;
                        }
                    }

                }
            }
        }

        List<Link> links = new ArrayList<>(n.getLinks().stream().toList());
        links.removeIf(link -> link.getSource().getID() == link.getTarget().getID());
    }


    /**
     * Adds the requested amount of mirrors to the network and connects them accordingly.
     *
     * @param n          the {@link Network}
     * @param newMirrors number of mirrors to add
     * @param props      {@link Properties} of the simulation
     * @param simTime    current simulation time
     */
    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {
        List<Mirror> mirrorsToAdd = createMirrors(newMirrors, simTime, props);
        n.getMirrors().addAll(mirrorsToAdd);
        //TODO: change to modifyNetworkAfterAddMirror
        restartNetwork(n, props, simTime);
    }

    @Override
    public void modifyNetworkAfterAddMirror(Network n, int newMirrors, Properties props, int simTime) {
        List<Mirror> mirrorsToAdd = createMirrors(newMirrors, simTime, props); //check if it returns only new added mirrors

        Set<Link> ret = new HashSet<>();
        for (Mirror m : mirrorsToAdd) {
            connectNewMirrorsToOther(n, props, m, ret);
        }
        n.getMirrors().addAll(mirrorsToAdd);
        n.getLinks().addAll(ret);
    }

    /**
     * Returns the number of links expected for the overall network according to this strategy.
     * If the number of mirrors is less than twice the number of links per mirror, we compute this like for the fully connected topology.
     * For a fully connected network this can be computed as (n * (n -1)) / 2, where n is the number of mirrors.
     * Else, the number of links can be simply computed by multiplying the number of links per mirror with the number of mirrors.
     *
     * @param n the {@link Network}
     * @return the number of links the network is expected to have
     */
    @Override
    public int getNumTargetLinks(Network n) {
        if (n.getNumMirrors() > 2 * n.getNumTargetLinksPerMirror())
            return n.getNumMirrors() * n.getNumTargetLinksPerMirror();
        else
            return (n.getNumMirrors() * n.getNumMirrors() - 1) / 2;
    }

    @Override
    public int getPredictedNumTargetLinks(Action a) {
        int m = a.getNetwork().getNumMirrors();
        int lpm = a.getNetwork().getNumTargetLinksPerMirror();
        if (a instanceof MirrorChange mc) {
            m += mc.getNewMirrors();
        } else if (a instanceof TargetLinkChange tlc) {
            lpm += tlc.getNewLinksPerMirror();
        }
        if (m > 2 * lpm) return m * lpm;
        else return (m * (m - 1)) / 2;
    }

}
