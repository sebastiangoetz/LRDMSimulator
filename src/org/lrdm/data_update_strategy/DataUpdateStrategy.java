package org.lrdm.data_update_strategy;


import org.lrdm.*;


/**Interface to be used by all DataUpdate strategies. Specifies methods to be used for updating the data and checking if the update is required.
 *
 */
public interface DataUpdateStrategy {

     /**Updates the data of a {@link Mirror}.
      *
      * @param m the {@link Mirror}, where the data is updated
      * @param n the {@link Network}, if the implementation needs more information to update
      * @return received bandwidth in one timestep
      */
     int updateData(Mirror m, Network n);

     /**Checks if the data of a {@link Mirror} needs to be updated.
      *
      * @param m the {@link Mirror}, where data update may be required
      * @param n the {@link Network}, if the implementation needs more information
      * @return true if the data update is required, false if not
      */
    boolean updateRequired(Mirror m, Network n);

}
