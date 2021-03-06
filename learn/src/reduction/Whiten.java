package reduction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import mat.Mat;
import mat.Vec;
import cmd.General;
import core.*;
import core.Utility.IntValue;

public class Whiten extends Machine {
    private double[][] _eigen_mat;
    private double[] _means;

    public Whiten() {
    }

    public void build() {
        OutFile.printf("transform the data input by Whitening to %d dimension\n",
                _n_outputs);

        _means = new double[_n_inputs];

    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = _n_inputs;

        build();

        // compute the mean values
        int n_examples = train_data._n_rows, i, j;
        double[][] cov = new double[_n_inputs][_n_inputs];

        double[] X = new double[_n_inputs];
        for (i = 0; i < n_examples; i++) {
            X = train_data.get_X(i);
            Vec.plus_equal(_means, X);
            // C += X X^T
            Mat.plus_equal(cov, Vec.outer_dot(X, X));

        }
        Vec.scale(_means, 1.0 / n_examples);

        // cov = \sum X X^T - n mean mean^T
        Mat.plus_scale(cov, -n_examples, Vec.outer_dot(_means, _means));
        Mat.scale(cov, 1.0 / (n_examples - 1));

        DoubleMatrix[] V = Eigen.symmetricEigenvectors(new DoubleMatrix(cov));

        _eigen_mat = new double[_n_inputs][_n_outputs];

        for (j = 0; j < _n_outputs; j++) {

            double scale = Math.sqrt(V[1].get(j, j) + 0.1);
            for (i = 0; i < _n_inputs; i++) {
                //V.set(i,j,V.get(i,j)/scale);
                _eigen_mat[i][j] = V[0].get(i, j) / scale;
            }
        }


//		DenseMatrix C = new DenseMatrix(_n_inputs, _n_inputs);
//		V.transBmult(V, C);
//		for (j = 0; j < _n_outputs; j++)
//		{
//			for (i = 0; i < _n_inputs; i++)
//			{
//				_eigen_mat[i][j] = C.get(i, j);
//			}
//		}


        return 0.0f;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        int i, j;
        double sum;
        for (j = 0; j < _n_outputs; j++) {
            sum = 0;
            for (i = 0; i < _n_inputs; i++) {
                sum += _eigen_mat[i][j] * (input[i] - _means[i]);
            }
            outputs[j] = sum;
        }

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _means = (double[]) in.readObject();
        _eigen_mat = (double[][]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_means);
        out.writeObject(_eigen_mat);
    }

}
