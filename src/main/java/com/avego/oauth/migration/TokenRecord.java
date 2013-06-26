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

/**
 * The TokenRecord represents a base class for token records
 * @version $Id$
 * @author conorroche
 */
public abstract class TokenRecord {

	private String tokenId;
	private byte[] token;

	/**
	 * This gets the tokenId
	 * @return the tokenId
	 */
	public String getTokenId() {
		return this.tokenId;
	}

	/**
	 * This sets the tokenId
	 * @param tokenId the tokenId to set
	 */
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	/**
	 * This gets the token
	 * @return the token
	 */
	public byte[] getToken() {
		return this.token;
	}

	/**
	 * This sets the token
	 * @param token the token to set
	 */
	public void setToken(byte[] token) {
		this.token = token;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.tokenId;
	}

}
