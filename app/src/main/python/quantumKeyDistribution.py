from concurrent import futures
futures.ProcessPoolExecutor = futures.ThreadPoolExecutor

import qiskit
from qiskit import *
from numpy.random import randint as int_random
import numpy as nump

q_regis = []
c_regis = []

def set_backend(b = 'qasm_simulator'):
    global backendX
    if b == 'ibmqx4' or b == 'ibmqx5':
        backendX = IBMQ.get_backend(b)
    elif b == 'ibmq_16_melbourne':
        backendX = IBMQ.get_backend(b)
    elif b == 'ibmq_qasm_simulator':
        backendX = IBMQ.get_backend(b)
    else:
        backendX = qiskit.BasicAer.get_backend('qasm_simulator')
    return "successful"


def encrypt_M(q_bits, bases_set):
    #         Creating an encoding function
    #         @q_bits - original_msg in q_bits
    #         @bases_set - Quantum gates
    #         returns original_msg
    original_msg = []
    for i in range(n):
        qr = qiskit.QuantumRegister(1)
        cr = qiskit.ClassicalRegister(1)
        q_regis.append(qr)
        c_regis.append(cr)
        quantumCir = qiskit.QuantumCircuit(qr, cr)
        if bases_set[i] == 0:  # Prepare qubit in Z-basis
            if q_bits[i] == 0:
                pass
            else:
                pass
                quantumCir.x(qr)
        else:  # Prepare qubit in X-basis
            if q_bits[i] == 0:
                quantumCir.h(qr)
            else:
                quantumCir.h(qr)
                quantumCir.x(qr)
        quantumCir.barrier()
        original_msg.append(quantumCir)
    return original_msg


nump.random.seed(seed=0)  # To generate randomness
n = 100  # Number of q_bits in original_msg
Tanmay_base = int_random(2, size=n)  # Tanmay chooses random basis for each bit
Tanmay_bit = int_random(2, size=n)  # original_msg q_bits (original q_bits)
# # print("Tanmay q_bits = " + str(Tanmay_bit))
original_msg = encrypt_M(Tanmay_bit, Tanmay_base)  # Encodes the q_bits according to the basis


def measure_original_msg(original_msg, bases_set):
    #         Measuring the computation from the
    #         @original_msg - original_msg q_bits
    #         @bases_set - quantum gates
    #         returns qbit_measurement

    backend = qiskit.BasicAer.get_backend('qasm_simulator') # Backend simulator
    qbit_measurement = []
    for q in range(n):
        if bases_set[q] == 0:  # measuring in Z-basis
            original_msg[q].measure(q_regis[q], c_regis[q])
        if bases_set[q] == 1:  # measuring in X-basis
            original_msg[q].h(q_regis[q])
            original_msg[q].measure(q_regis[q], c_regis[q])
        # result = qiskit.execute(original_msg[q], backend)
        # # execute the quantum circuit on a IBM quantum computer
        # measured_bit = int(result.get_memory()[0])
        # qbit_measurement.append(measured_bit)
    return qbit_measurement


# If there is no eavesdropping and Divang measures the q_bits
Divang_base = int_random(2, size=n)  # Divang chooses random basis for each bit
Divang_result = measure_original_msg(original_msg,Divang_base)  # Divang measures the q_bits according to his basis



def discard_q_bits(Tanmay_bases_set, Divang_bases_set, q_bits):
    #         Discards the uncommon bases_set
    #         @Tanmay_bases_set - Tanmay's base
    #         @Divang_bases_set - Divang's base
    #         @q_bits - original_msg
    #         returns good_q_bits
    good_q_bits = []
    for q in range(n):
        if Tanmay_bases_set[q] == Divang_bases_set[q]:
            # If  the basis of Tanmay & Divang matches, add
            # this to 'good' q_bits
            good_q_bits.append(q_bits[q])
    return good_q_bits


# Now we discard the q_bits which have different basis with respect to Tanmay & Divang
# and store them as their keys respectively
# Tanmay_key = discard_q_bits(Tanmay_base, Divang_base, Tanmay_bit)
# Divang_key = discard_q_bits(Tanmay_base, Divang_base, Divang_result)


def q_bits_selection(q_bits, selection):
    #         Measures the random sam q_bits
    #         @q_bits - original_msg
    #         @selection - good_q_bits
    #         returns sam
    sam = []
    for idx in selection:
        # taking modulus
        idx = nump.mod(idx, len(q_bits))
        sam.append(q_bits.pop(idx))
    return sam


# Now we take random indices of a fixed sam size to compare their respective keys
# sam = 10
# selected_bits = int_random(n, size=sam)  # random indices
# Tanmay_sam = q_bits_selection(Tanmay_key, selected_bits)
# Divang_sam = q_bits_selection(Divang_key, selected_bits)


def listToString(s):
    str1 = ""

    for ele in s:
        str1 += ele

    return str1


def getTanmayKey():
    return listToString(Tanmay_sam)


def getDivangKey():
    return listToString(Divang_sam)