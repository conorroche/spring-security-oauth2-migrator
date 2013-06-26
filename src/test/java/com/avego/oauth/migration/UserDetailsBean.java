/*
 * Copyright © 2013 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avego.oauth.migration;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * The UserDetailsBean represents a simple bean impl of spring sec user detail
 * @version $Id$
 * @author conorroche
 */
public class UserDetailsBean implements org.springframework.security.core.userdetails.UserDetails, Serializable {

	/**
	 * This is the serial uid
	 */
	private static final long serialVersionUID = 1L;

	private Collection<? extends GrantedAuthority> authorities;
	private String password;
	private String username;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;

	/**
	 * This gets the authorities
	 * @return the authorities
	 */
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	/**
	 * This sets the authorities
	 * @param authorities the authorities to set
	 */
	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	/**
	 * This gets the password
	 * @return the password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * This sets the password
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * This gets the username
	 * @return the username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * This sets the username
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * This gets the accountNonExpired
	 * @return the accountNonExpired
	 */
	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	/**
	 * This sets the accountNonExpired
	 * @param accountNonExpired the accountNonExpired to set
	 */
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	/**
	 * This gets the accountNonLocked
	 * @return the accountNonLocked
	 */
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	/**
	 * This sets the accountNonLocked
	 * @param accountNonLocked the accountNonLocked to set
	 */
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	/**
	 * This gets the credentialsNonExpired
	 * @return the credentialsNonExpired
	 */
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	/**
	 * This sets the credentialsNonExpired
	 * @param credentialsNonExpired the credentialsNonExpired to set
	 */
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	/**
	 * This gets the enabled
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * This sets the enabled
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
