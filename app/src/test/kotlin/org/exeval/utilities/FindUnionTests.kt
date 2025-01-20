package org.exeval.utilities

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FindUnionTest {
	private lateinit var findUnion: FindUnion

	@BeforeEach
	fun setUp() {
		// Initialize the FindUnion class with 10 elements
		findUnion = FindUnion(10)
	}

	@Test
	fun testFindSingleElement() {
		// Test that a single element's root is itself
		assertEquals(0, findUnion.find(0))
		assertEquals(1, findUnion.find(1))
	}

	@Test
	fun testUnionAndFind() {
		// Union two elements and check if their roots are the same
		findUnion.union(0, 1)
		assertEquals(findUnion.find(0), findUnion.find(1))

		// Test union of another pair
		findUnion.union(2, 3)
		assertEquals(findUnion.find(2), findUnion.find(3))

		// Test that different sets are not connected
		assertNotEquals(findUnion.find(0), findUnion.find(2))
	}

	@Test
	fun testPathCompression() {
		// Perform several unions
		findUnion.union(0, 1)
		findUnion.union(1, 2)
		findUnion.union(2, 3)

		// After all unions, 0, 1, 2, 3 should all have the same root
		assertEquals(findUnion.find(0), findUnion.find(3))
		assertEquals(findUnion.find(1), findUnion.find(3))
		assertEquals(findUnion.find(2), findUnion.find(3))
	}

	@Test
	fun testUnionByRank() {
		// Union elements with union by rank consideration
		findUnion.union(0, 1) // Rank of 0, 1 will be 1
		findUnion.union(1, 2) // Rank of 1 should remain higher

		// Assert that element 0 and 2 have the same root, but rank should be maintained
		assertEquals(findUnion.find(0), findUnion.find(2))

		// Now test rank: 1 should be attached to 0 due to rank consideration
		findUnion.union(0, 3)
		assertEquals(findUnion.find(1), findUnion.find(3))
	}

	@Test
	fun testMultipleUnions() {
		// Union multiple elements
		findUnion.union(0, 1)
		findUnion.union(1, 2)
		findUnion.union(3, 4)
		findUnion.union(4, 5)

		// Test that elements within each set are connected
		assertEquals(findUnion.find(0), findUnion.find(1))
		assertEquals(findUnion.find(1), findUnion.find(2))

		// Test that elements from different sets are not connected
		assertNotEquals(findUnion.find(0), findUnion.find(3))
	}

	@Test
	fun testNoUnion() {
		// Test that no union results in different roots
		assertNotEquals(findUnion.find(0), findUnion.find(1))
		assertNotEquals(findUnion.find(2), findUnion.find(3))
	}

	@Test
	fun testUnionAfterPathCompression() {
		// Perform a union and then check after path compression
		findUnion.union(0, 1)
		findUnion.union(1, 2)
		findUnion.find(0) // Perform find to trigger path compression
		findUnion.union(2, 3)

		// Check that all elements are in the same set after path compression
		assertEquals(findUnion.find(0), findUnion.find(3))
	}

	@Test
	fun testMultipleUnionsAndFinds() {
		// Multiple unions and checking each find
		findUnion.union(0, 1)
		findUnion.union(1, 2)
		assertEquals(findUnion.find(0), findUnion.find(2))

		findUnion.union(3, 4)
		assertNotEquals(findUnion.find(0), findUnion.find(4))

		findUnion.union(2, 3)
		assertEquals(findUnion.find(0), findUnion.find(4))
	}
}
