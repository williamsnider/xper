#include <chrono>
#include <iostream>
#include <string>
#include <zmq.hpp>
#include <thread>
using namespace std;

int main() {

  // initialize the zmq context with a single IO thread
  zmq::context_t context{1};

  // construct socket
  zmq::socket_t socket(context, zmq::socket_type::req);
  socket.connect("tcp://127.0.0.1:8888");

  // string pub_msg = "greetings from C++";
  int i = 0;
  for (;; i++) {
    std::string pub_msg =
        "{\"O_T_EE\": "
        "[0.999985,0.000330051,-0.00320673,0,0.000330898,-0.99999,0.000263483,0,-0.00320668,-0."
        "000264546,-0.999995,0,0.307213,-6.58702e-05,0.4857,1], \"O_T_EE_d\": "
        "[0.7071,0.7071,0,0,0.7071,-0.7071,0,0,0,0,-1,0,0.088,0,0.8226,1], \"F_T_NE\": "
        "[0.7071,-0.7071,0,0,0.7071,0.7071,0,0,0,0,1,0,0,0,0.1034,1], \"NE_T_EE\": "
        "[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1], \"F_T_EE\": "
        "[0.7071,-0.7071,0,0,0.7071,0.7071,0,0,0,0,1,0,0,0,0.1034,1], \"EE_T_K\": "
        "[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1], \"m_ee\": 0.73, \"F_x_Cee\": [-0.01,0,0.03], \"I_ee\": "
        "[0.001,0,0,0,0.0025,0,0,0,0.0017], \"m_load\": 0, \"F_x_Cload\": [0,0,0], \"I_load\": "
        "[0,0,0,0,0,0,0,0,0], \"m_total\": 0.73, \"F_x_Ctotal\": [-0.01,0,0.03], \"I_total\": "
        "[0.001,0,0,0,0.0025,0,0,0,0.0017], \"elbow\": [6.13345e-05,-1], \"elbow_d\": [0,1], "
        "\"elbow_c\": [0,0], \"delbow_c\": [0,0], \"ddelbow_c\": [0,0], \"tau_J\": "
        "[0.088807,-4.117,-0.600776,22.2297,0.905837,2.1786,0.115023], \"tau_J_d\": "
        "[0,0,0,0,0,0,0], \"dtau_J\": "
        "[-47.3788,-68.3469,20.5651,45.0312,-11.326,-17.7876,-98.1079], \"q\": "
        "[-0.000127672,-0.782856,6.13345e-05,-2.35709,-0.000221556,1.57102,0.784984], \"dq\": "
        "[0.000757043,-7.4699e-05,-0.000223402,-0.00111917,0.000278315,-0.000274795,0.0023821], "
        "\"q_d\": [0,0,0,0,0,0,0], \"dq_d\": [0,0,0,0,0,0,0], \"ddq_d\": [0,0,0,0,0,0,0], "
        "\"joint_contact\": [0,0,0,0,0,0,0], \"cartesian_contact\": [0,0,0,0,0,0], "
        "\"joint_collision\": [0,0,0,0,0,0,0], \"cartesian_collision\": [0,0,0,0,0,0], "
        "\"tau_ext_hat_filtered\": "
        "[0.116015,-0.119609,0.0923087,0.331439,0.147008,-0.0684078,0.165774], \"O_F_ext_hat_K\": "
        "[0.129032,0.7117,1.06763,-0.33018,-0.0765281,0.0749857], \"K_F_ext_hat_K\": "
        "[0.125844,-0.711383,-1.06822,0.0160867,-0.188819,0.143566], \"O_dP_EE_d\": [0,0,0,0,0,0], "
        "\"O_ddP_O\": [0,0,-9.81], \"O_T_EE_c\": [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0], \"O_dP_EE_c\": "
        "[0,0,0,0,0,0], \"O_ddP_EE_c\": [0,0,0,0,0,0], \"theta\": "
        "[-0.00012144,-0.783145,1.91748e-05,-2.35553,-0.000119842,1.57127,0.784995], \"dtheta\": "
        "[0,0,0,0,0,0,0], \"current_errors\": [], \"last_motion_errors\": [], "
        "\"control_command_success_rate\": 0, \"robot_mode\": \"Other\", \"time\": 3412939}";
    zmq::message_t message_to_send(pub_msg.size());
    memcpy(message_to_send.data(), pub_msg.c_str(), pub_msg.size());
    socket.send(message_to_send, zmq::send_flags::dontwait);
    auto start = std::chrono::steady_clock::now();
    std::cout << "Robot data sent. " << i << std::endl;


    //  Wait for next request from client
    zmq::message_t request;
    socket.recv (request, zmq::recv_flags::none);
    auto stop = std::chrono::steady_clock::now();
    auto duration = std::chrono::duration_cast<chrono::microseconds>(stop - start);
    std::cout << "Received reply. RTT = " << duration.count() << "us" << std::endl;

    // Pause
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
  }
}