############################################################################
##
##     This file is part of Purdue CS 536.
##
##     Purdue CS 536 is free software: you can redistribute it and/or modify
##     it under the terms of the GNU General Public License as published by
##     the Free Software Foundation, either version 3 of the License, or
##     (at your option) any later version.
##
##     Purdue CS 536 is distributed in the hope that it will be useful,
##     but WITHOUT ANY WARRANTY; without even the implied warranty of
##     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##     GNU General Public License for more details.
##
##     You should have received a copy of the GNU General Public License
##     along with Purdue CS 536. If not, see <https://www.gnu.org/licenses/>.
##
#############################################################################

####################################################################
############### Set up Mininet and Controller ######################
####################################################################

SCRIPTS = ./scripts

export topo ?= single,3
export name ?=

.PHONY: mininet controller cli netcfg host-h1 host-h2 host-h3

help: 
	@echo "Example usage:"
	@echo "- Start Mininet: make mininet [topo=single,3]"
	@echo "- Start Controller (ONOS): make controller"
	@echo "- Launch ONOS CLI: make cli"
	@echo "- Apply net config: make netcfg"
	@echo "- Access Host: make host name={name}\n"
	@echo "To Start as designed, perform:"
	@echo "- Run 'make mininet' in new terminal"
	@echo "- Run 'make controller' in new terminal"
	@echo "- Run 'make cli' in new terminal, and inside the cli 'app activate fwd'"
	@echo "- Run 'make netcfg' in new terminal"
	@echo "- Run 'make host name={name}' to access hosts.\n"

mininet:
	$(SCRIPTS)/mn-stratum --topo $(topo)

controller:
	ONOS_APPS=gui,proxyarp,drivers.bmv2,lldpprovider,hostprovider \
	$(SCRIPTS)/onos

cli:
	$(SCRIPTS)/onos-cli

netcfg:
	$(SCRIPTS)/onos-netcfg cfg/netcfg.json

host:
	$(SCRIPTS)/utils/mn-stratum/exec $(name)
    
