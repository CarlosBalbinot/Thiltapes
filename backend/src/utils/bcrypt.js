import bcrypt from 'bcrypt';

/**
 * Hashes a plain text password using bcrypt.
 * * @param {string} password - The plain text password to be hashed.
 * @param {number} [saltRounds=10] - The cost factor for the bcrypt algorithm.
 * @returns {Promise<string>} A promise that resolves to the hashed password.
 */
export const hashPassword = async (password) => {
  return await bcrypt.hash(password, 10);
};

/**
 * Compares a plain text password against a stored bcrypt hash.
 * * @param {string} plainPassword - The plain text password to check.
 * @param {string} hashedPassword - The stored bcrypt hash.
 * @returns {Promise<boolean>} A promise that resolves to true if they match, false otherwise.
 */
export const verifyPassword = async (plainPassword, hashedPassword) => {
  return await bcrypt.compare(plainPassword, hashedPassword);
};