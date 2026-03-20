/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/**
 * Base API Service class providing common HTTP methods and error handling
 */
class BaseService {
  constructor(apiModule = null) {
    this.apiModule = apiModule;
  }

  /**
   * Make a GET request
   * @param {string} endpoint - API endpoint
   * @param {Object} params - Query parameters
   * @param {Object} config - Additional axios config
   * @returns {Promise<any>}
   */
  async get(endpoint, params = {}, config = {}) {
    try {
      const url = this._buildUrl(endpoint);
      return await this.$axiosGet(url, params, config);
    } catch (error) {
      return this._handleError(error, { endpoint, params, method: 'GET' });
    }
  }

  /**
   * Make a POST request with JSON payload
   * @param {string} endpoint - API endpoint
   * @param {Object} data - Request body
   * @param {Object} config - Additional axios config
   * @returns {Promise<any>}
   */
  async post(endpoint, data = {}, config = {}) {
    try {
      const url = this._buildUrl(endpoint);
      return await this.$axiosJsonPost(url, data, config);
    } catch (error) {
      return this._handleError(error, { endpoint, data, method: 'POST' });
    }
  }

  /**
   * Make a POST request with FormData payload
   * @param {string} endpoint - API endpoint
   * @param {Object} data - Form data
   * @param {Object} config - Additional axios config
   * @returns {Promise<any>}
   */
  async postForm(endpoint, data = {}, config = {}) {
    try {
      const url = this._buildUrl(endpoint);
      return await this.$axiosPost(url, data, config);
    } catch (error) {
      return this._handleError(error, { endpoint, data, method: 'POST_FORM' });
    }
  }

  /**
   * Make a POST request for file upload
   * @param {string} endpoint - API endpoint
   * @param {FormData} formData - FormData with files
   * @param {Object} config - Additional axios config
   * @returns {Promise<any>}
   */
  async upload(endpoint, formData, config = {}) {
    try {
      const url = this._buildUrl(endpoint);
      return await this.$axiosPostUpload(url, formData, config);
    } catch (error) {
      return this._handleError(error, { endpoint, method: 'UPLOAD' });
    }
  }

  /**
   * Build full URL for API endpoint
   * @private
   */
  _buildUrl(endpoint) {
    // If endpoint is a function (e.g., from global.API), call it with any arguments
    if (typeof endpoint === 'function') {
      return endpoint();
    }
    return endpoint;
  }

  /**
   * Handle API errors with consistent logging and formatting
   * @private
   */
  _handleError(error, context = {}) {
    console.error(`API Error [${context.method || 'UNKNOWN'}] ${context.endpoint || 'unknown endpoint'}:`, {
      error,
      context,
      timestamp: new Date().toISOString()
    });

    // Re-throw with additional context
    const enhancedError = new Error(`API call failed: ${context.endpoint || 'unknown'}`);
    enhancedError.originalError = error;
    enhancedError.context = context;
    throw enhancedError;
  }

  /**
   * Set axios methods from Vue prototype
   * This should be called during Vue initialization
   */
  static setAxiosMethods(Vue) {
    BaseService.prototype.$axiosGet = Vue.prototype.$axiosGet;
    BaseService.prototype.$axiosPost = Vue.prototype.$axiosPost;
    BaseService.prototype.$axiosJsonPost = Vue.prototype.$axiosJsonPost;
    BaseService.prototype.$axiosPostUpload = Vue.prototype.$axiosPostUpload;
  }
}

export default BaseService;